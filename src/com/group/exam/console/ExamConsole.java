package com.group.exam.console;

import com.group.exam.common.ConsoleKit;
import com.group.exam.entity.AnswerRecord;
import com.group.exam.entity.PaperItem;
import com.group.exam.entity.Question;
import com.group.exam.entity.User;
import com.group.exam.service.ExamService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线考试控制台：逐题作答、交卷与成绩单展示。
 */
public final class ExamConsole {

    private static final ExamService EXAM_SERVICE = new ExamService();

    private ExamConsole() {
    }

    /**
     * 开始并完成一场考试。
     */
    public static void start(User student, int paperId) {
        ExamService.ExamContext ctx = EXAM_SERVICE.startExam(student, paperId);
        int sessionId = ctx.session.getSessionId();

        System.out.println("\n========== 考试开始 ==========");
        System.out.println("试卷：" + ctx.paperName + " | 共 " + ctx.items.size()
                + " 题 | 时长 " + ctx.durationMinutes + " 分钟");
        System.out.println("会话编号：" + sessionId + "。请逐题作答，答完后交卷。");

        Map<Integer, String> answers = new LinkedHashMap<Integer, String>();
        for (PaperItem item : ctx.items) {
            Question q = item.getQuestion();
            System.out.println("第 " + item.getPaperQuestion().getQuestionOrder() + " 题（"
                    + item.getPaperQuestion().getScore() + " 分）");
            System.out.println(q.toDisplay());
            String answer = ConsoleKit.readLine(q.answerTip());
            while (!q.isValidAnswer(answer)) {
                System.out.println("【提示】答案格式不正确，请重新输入。");
                answer = ConsoleKit.readLine(q.answerTip());
            }
            answers.put(q.getQuestionId(), answer);
        }

        ExamService.SubmitResult result = EXAM_SERVICE.submitExam(sessionId, answers, false);
        printReport(ctx, result);
    }

    /** 打印成绩单 */
    private static void printReport(ExamService.ExamContext ctx, ExamService.SubmitResult result) {
        System.out.println("\n========== 成绩单 ==========");
        System.out.println("试卷：" + ctx.paperName
                + "（已交卷）");
        System.out.println("本次得分：" + result.totalScore + " 分"
                + "（满分 " + ctx.items.stream()
                .mapToDouble(item -> item.getPaperQuestion().getScore()).sum() + " 分）");
        System.out.println("判定：" + (result.passed ? "及格" : "不及格") + "（及格线 60 分）");

        List<AnswerRecord> records = EXAM_SERVICE.answerRecords(ctx.session.getSessionId());
        if (!records.isEmpty()) {
            System.out.println("---------- 逐题结果 ----------");
            for (AnswerRecord r : records) {
                System.out.println(r.getQuestionContent()
                        + " | 你的答案：" + (r.getUserAnswer().isEmpty() ? "（未作答）" : r.getUserAnswer())
                        + " | 正确答案：" + r.getCorrectAnswer()
                        + " | " + (r.getCorrect() == 1 ? "√ " : "× ")
                        + r.getScore() + " 分");
            }
        }
        System.out.println("============================");
    }
}
