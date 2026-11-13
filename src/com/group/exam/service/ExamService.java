package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.common.Constants;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.AnswerRecordDao;
import com.group.exam.dao.ExamSessionDao;
import com.group.exam.entity.AnswerRecord;
import com.group.exam.entity.ExamSession;
import com.group.exam.entity.PaperItem;
import com.group.exam.entity.User;
import com.group.exam.service.grading.GradingContext;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 考试服务：开始考试、交卷判分。
 *
 * <p>多线程技术的落实：</p>
 * <ol>
 *   <li>判分阶段使用固定线程池并行判分（Executors + Future）；</li>
 *   <li>成绩写库使用 synchronized 保证并发场景下的数据一致。</li>
 * </ol>
 */
public class ExamService {

    private static final GradingContext GRADING_CONTEXT = new GradingContext();

    private final ExamSessionDao sessionDao = DaoFactory.examSessionDao();
    private final AnswerRecordDao answerRecordDao = DaoFactory.answerRecordDao();
    private final PaperService paperService = new PaperService();

    /**
     * 一场考试的运行时上下文：会话、题目。
     */
    public static class ExamContext {
        public ExamSession session;
        public List<PaperItem> items;
        public String paperName;
        public int durationMinutes;
    }

    /**
     * 交卷结果：总分、是否及格、是否超时、答题明细。
     */
    public static class SubmitResult {
        public double totalScore;
        public boolean passed;
        public boolean timeout;
        public List<AnswerRecord> records;
    }

    /**
     * 开始考试：校验试卷已发布、无未完成会话，并创建会话。
     */
    public ExamContext startExam(User student, int paperId) {
        PaperService.PaperDetail detail = paperService.detail(paperId);
        if (!Constants.PAPER_PUBLISHED.equals(detail.getPaper().getStatus())) {
            throw new BizException("该试卷尚未发布，不能参加考试");
        }
        if (detail.getItems().isEmpty()) {
            throw new BizException("该试卷没有题目，请联系教师处理");
        }
        try {
            ExamSession active = sessionDao.findActiveByStudentAndPaper(student.getUserId(), paperId);
            if (active != null) {
                throw new BizException("您有一场未完成的考试（会话编号 " + active.getSessionId()
                        + "），请先完成它");
            }
            ExamSession session = new ExamSession();
            session.setPaperId(paperId);
            session.setStudentId(student.getUserId());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            session.setStartTime(now.toString());
            session.setEndTime(new Timestamp(now.getTime()
                    + detail.getPaper().getDurationMinutes() * 60L * 1000L).toString());
            session.setStatus(Constants.SESSION_IN_PROGRESS);
            int sessionId = sessionDao.insert(session);
            session.setSessionId(sessionId);

            ExamContext ctx = new ExamContext();
            ctx.session = session;
            ctx.items = detail.getItems();
            ctx.paperName = detail.getPaper().getPaperName();
            ctx.durationMinutes = detail.getPaper().getDurationMinutes();

            return ctx;
        } catch (SQLException e) {
            throw new BizException("创建考试会话失败：" + e.getMessage(), e);
        }
    }

    /**
     * 交卷：并行判分 -> 汇总成绩 -> 写入答题明细与成绩。
     *
     * @param sessionId 会话编号
     * @param answers   题目ID -> 学生答案
     * @param timeout   是否超时交卷
     */
    public SubmitResult submitExam(int sessionId, Map<Integer, String> answers, boolean timeout) {
        try {
            ExamSession session = sessionDao.findById(sessionId);
            if (session == null) {
                throw new BizException("考试会话不存在");
            }
            if (!Constants.SESSION_IN_PROGRESS.equals(session.getStatus())) {
                throw new BizException("该考试已交卷，不能重复提交");
            }
            List<PaperItem> items = paperService.detail(session.getPaperId()).getItems();

            // 并行判分：固定线程池 + Future
            ExecutorService pool = Executors.newFixedThreadPool(
                    Math.max(1, Math.min(4, items.size())));
            List<Future<AnswerRecord>> futures = new ArrayList<Future<AnswerRecord>>();
            for (final PaperItem item : items) {
                futures.add(pool.submit(() -> {
                    int questionId = item.getQuestion().getQuestionId();
                    String userAnswer = answers.get(questionId) == null ? "" : answers.get(questionId);
                    double gained = GRADING_CONTEXT.grade(item.getQuestion(), userAnswer);
                    AnswerRecord record = new AnswerRecord();
                    record.setSessionId(sessionId);
                    record.setQuestionId(questionId);
                    record.setUserAnswer(userAnswer);
                    record.setCorrect(gained > 0 ? 1 : 0);
                    record.setScore(gained);
                    return record;
                }));
            }
            pool.shutdown();

            double total = 0;
            List<AnswerRecord> records = new ArrayList<AnswerRecord>();
            for (Future<AnswerRecord> future : futures) {
                try {
                    AnswerRecord record = future.get();
                    total += record.getScore();
                    records.add(record);
                } catch (Exception e) {
                    throw new BizException("判分过程出现异常", e);
                }
            }

            // 同步写库：synchronized 保证并发交卷时成绩计算与写入一致
            int passed = 0;
            synchronized (ExamService.class) {
                answerRecordDao.batchInsert(records);
                String status = timeout ? Constants.SESSION_TIMEOUT : Constants.SESSION_SUBMITTED;
                String submitTime = new Timestamp(System.currentTimeMillis()).toString();
                passed = total >= Constants.PASS_SCORE ? 1 : 0;
                sessionDao.updateSubmitResult(sessionId, status, total, passed, submitTime);
            }

            SubmitResult result = new SubmitResult();
            result.totalScore = total;
            result.passed = passed == 1;
            result.timeout = timeout;
            result.records = records;
            return result;
        } catch (SQLException e) {
            throw new BizException("交卷失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询某次考试的逐题判分结果（联表带出题干与正确答案）。
     */
    public List<AnswerRecord> answerRecords(int sessionId) {
        try {
            return answerRecordDao.findBySessionId(sessionId);
        } catch (SQLException e) {
            throw new BizException("查询答题明细失败：" + e.getMessage(), e);
        }
    }
}
