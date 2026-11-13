package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.common.Constants;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.PaperDao;
import com.group.exam.dao.PaperQuestionDao;
import com.group.exam.entity.ExamPaper;
import com.group.exam.entity.PaperItem;
import com.group.exam.entity.PaperQuestion;
import com.group.exam.entity.Question;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 试卷服务：创建试卷、随机组卷、发布、试卷详情。
 */
public class PaperService {

    private final PaperDao paperDao = DaoFactory.paperDao();
    private final PaperQuestionDao paperQuestionDao = DaoFactory.paperQuestionDao();
    private final QuestionService questionService = new QuestionService();

    /**
     * 创建草稿试卷。
     */
    public ExamPaper create(String paperName, String subject, int durationMinutes, int createdBy) {
        if (paperName == null || paperName.trim().isEmpty()) {
            throw new BizException("试卷名称不能为空");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new BizException("科目不能为空");
        }
        if (durationMinutes <= 0 || durationMinutes > 180) {
            throw new BizException("考试时长必须为 1-180 分钟");
        }
        try {
            ExamPaper paper = new ExamPaper();
            paper.setPaperName(paperName.trim());
            paper.setSubject(subject.trim());
            paper.setDurationMinutes(durationMinutes);
            paper.setCreatedBy(createdBy);
            paper.setStatus(Constants.PAPER_DRAFT);
            int paperId = paperDao.insert(paper);
            paper.setPaperId(paperId);
            return paper;
        } catch (SQLException e) {
            throw new BizException("创建试卷失败：" + e.getMessage(), e);
        }
    }

    /**
     * 随机组卷：按题型与数量从题库随机抽题，计算总分并事务化写入。
     *
     * @param paperId 试卷编号（须为草稿且未组卷）
     * @param rules   题型 -> 抽取数量（如 SINGLE=5）
     * @return 生成的题目条目
     */
    public List<PaperItem> generateRandom(int paperId, Map<String, Integer> rules) {
        try {
            ExamPaper paper = paperDao.findById(paperId);
            if (paper == null) {
                throw new BizException("试卷不存在");
            }
            if (!Constants.PAPER_DRAFT.equals(paper.getStatus())) {
                throw new BizException("仅草稿状态的试卷可以组卷");
            }
            if (paperQuestionDao.countByPaperId(paperId) > 0) {
                throw new BizException("该试卷已完成组卷，如需重新组卷请新建试卷");
            }
            int order = 1;
            double total = 0;
            List<PaperQuestion> questions = new ArrayList<PaperQuestion>();
            List<PaperItem> items = new ArrayList<PaperItem>();
            for (Map.Entry<String, Integer> entry : rules.entrySet()) {
                List<Question> picked = questionService.randomByType(entry.getKey(), entry.getValue());
                for (Question q : picked) {
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paperId);
                    pq.setQuestionId(q.getQuestionId());
                    pq.setQuestionOrder(order++);
                    pq.setScore(q.getScore());
                    questions.add(pq);
                    items.add(new PaperItem(pq, q));
                    total += q.getScore();
                }
            }
            if (questions.isEmpty()) {
                throw new BizException("组卷规则不能为空，请至少为一种题型设置抽取数量");
            }
            paper.setTotalScore(total);
            paperDao.attachQuestionsAndSetTotal(paperId, total, questions);
            return items;
        } catch (SQLException e) {
            throw new BizException("组卷失败：" + e.getMessage(), e);
        }
    }

    /**
     * 发布试卷：仅允许对已组卷的草稿试卷发布。
     */
    public void publish(int paperId) {
        try {
            ExamPaper paper = paperDao.findById(paperId);
            if (paper == null) {
                throw new BizException("试卷不存在");
            }
            if (Constants.PAPER_PUBLISHED.equals(paper.getStatus())) {
                throw new BizException("该试卷已经发布");
            }
            if (paperQuestionDao.countByPaperId(paperId) == 0) {
                throw new BizException("试卷为空，请先完成组卷再发布");
            }
            paperDao.updateStatus(paperId, Constants.PAPER_PUBLISHED);
        } catch (SQLException e) {
            throw new BizException("发布试卷失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询试卷列表。
     *
     * @param statusFilter 状态过滤，null 或空表示全部
     */
    public List<ExamPaper> list(String statusFilter) {
        try {
            return paperDao.findAll(statusFilter);
        } catch (SQLException e) {
            throw new BizException("查询试卷失败：" + e.getMessage(), e);
        }
    }

    /**
     * 试卷详情：试卷基本信息 + 按题序排列的题目条目。
     */
    public PaperDetail detail(int paperId) {
        try {
            ExamPaper paper = paperDao.findById(paperId);
            if (paper == null) {
                throw new BizException("试卷不存在");
            }
            List<PaperQuestion> pqs = paperQuestionDao.findByPaperId(paperId);
            Map<Integer, Question> questionMap = new LinkedHashMap<Integer, Question>();
            List<Integer> ids = new ArrayList<Integer>();
            for (PaperQuestion pq : pqs) {
                ids.add(pq.getQuestionId());
            }
            List<Question> questions = DaoFactory.questionDao().findByIds(ids);
            for (Question q : questions) {
                questionMap.put(q.getQuestionId(), q);
            }
            List<PaperItem> items = new ArrayList<PaperItem>();
            for (PaperQuestion pq : pqs) {
                Question q = questionMap.get(pq.getQuestionId());
                if (q != null) {
                    items.add(new PaperItem(pq, q));
                }
            }
            return new PaperDetail(paper, items);
        } catch (SQLException e) {
            throw new BizException("查询试卷详情失败：" + e.getMessage(), e);
        }
    }

    /**
     * 试卷详情结果封装。
     */
    public static class PaperDetail {
        private final ExamPaper paper;
        private final List<PaperItem> items;

        public PaperDetail(ExamPaper paper, List<PaperItem> items) {
            this.paper = paper;
            this.items = items;
        }

        public ExamPaper getPaper() {
            return paper;
        }

        public List<PaperItem> getItems() {
            return items;
        }
    }
}
