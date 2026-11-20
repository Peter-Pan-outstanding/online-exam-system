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
 * 在线考试控制台：逐题作答、答题卡总览、跳题修改、交卷二次确认、
 * 成绩单与解析展示。
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
        System.out.println("会话编号：" + sessionId + "。");
        System.out.println("作答提示：输入答案直接作答；输入 V 查看答题卡；输入 J 跳题修改；输入 S 交卷。");

        Map<Integer, String> answers = new LinkedHashMap<Integer, String>();
        int total = ctx.items.size();
        int current = 0;

        while (true) {
            PaperItem item = ctx.items.get(current);
            Question q = item.getQuestion();
            System.out.println("第 " + (current + 1) + " / " + total + " 题（"
                    + item.getPaperQuestion().getScore() + " 分）");
            System.out.println(q.toDisplay());

            String input = ConsoleKit.readLine("作答（或 V/J/S）：").trim();
            if (input.isEmpty()) {
                // 空输入视为跳过本题（可稍后通过答题卡/跳题补充）
                answers.remove(q.getQuestionId());
                current = moveNext(ctx, answers, current, total);
                if (current < 0) {
                    return;
                }
                continue;
            }
            String cmd = input.toUpperCase();
            if ("V".equals(cmd)) {
                showAnswerSheet(ctx, answers);
                continue;
            }
            if ("J".equals(cmd)) {
                current = ConsoleKit.readIntRange("跳转到第几题（1-" + total + "）：", 1, total) - 1;
                continue;
            }
            if ("S".equals(cmd)) {
                showAnswerSheet(ctx, answers);
                if (confirmSubmit()) {
                    submitAndBreak(ctx, answers, sessionId);
                    return;
                }
                continue;
            }
            if (!q.isValidAnswer(input)) {
                System.out.println("【提示】答案格式不正确：" + q.answerTip());
                continue;
            }
            answers.put(q.getQuestionId(), input);
            current = moveNext(ctx, answers, current, total);
            if (current < 0) {
                return;
            }
        }

    }

    /**
     * 进入下一题；到达末尾时提示交卷或返回修改。
     *
     * @return 下一题下标；-1 表示已交卷退出
     */
    private static int moveNext(ExamService.ExamContext ctx, Map<Integer, String> answers,
                                int current, int total) {
        if (current + 1 < total) {
            return current + 1;
        }
        showAnswerSheet(ctx, answers);
        System.out.println("您已浏览完全部题目。");
        if (confirmSubmit()) {
            submitAndBreak(ctx, answers, ctx.session.getSessionId());
            return -1;
        }
        return firstUnanswered(ctx, answers);
    }

    /** 返回第一个未答题号；全部已答返回 0 */
    private static int firstUnanswered(ExamService.ExamContext ctx, Map<Integer, String> answers) {
        for (int i = 0; i < ctx.items.size(); i++) {
            int qid = ctx.items.get(i).getQuestion().getQuestionId();
            String ans = answers.get(qid);
            if (ans == null || ans.isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    /** 答题卡总览 */
    private static void showAnswerSheet(ExamService.ExamContext ctx, Map<Integer, String> answers) {
        int total = ctx.items.size();
        int answered = 0;
        StringBuilder unanswered = new StringBuilder();
        for (int i = 0; i < total; i++) {
            int qid = ctx.items.get(i).getQuestion().getQuestionId();
            String ans = answers.get(qid);
            if (ans == null || ans.isEmpty()) {
                if (unanswered.length() > 0) {
                    unanswered.append(", ");
                }
                unanswered.append(i + 1);
            } else {
                answered++;
            }
        }
        System.out.println("\n---------- 答题卡 ----------");
        System.out.println("已答 " + answered + " / " + total + " 题。");
        if (unanswered.length() == 0) {
            System.out.println("所有题目均已作答。");
        } else {
            System.out.println("未答题号：" + unanswered);
        }
        System.out.println("提示：输入 J 跳转到指定题目修改，输入 S 交卷。");
        System.out.println("--------------------------");
    }

    /** 交卷二次确认 */
    private static boolean confirmSubmit() {
        String first = ConsoleKit.readLine("确认交卷？(y/n)：");
        if (!"y".equalsIgnoreCase(first)) {
            System.out.println("已取消交卷，继续作答。");
            return false;
        }
        String second = ConsoleKit.readLine("再次确认，交卷后不可修改！(y/n)：");
        if (!"y".equalsIgnoreCase(second)) {
            System.out.println("已取消交卷，继续作答。");
            return false;
        }
        return true;
    }

    /** 提交并打印成绩单 */
    private static void submitAndBreak(ExamService.ExamContext ctx, Map<Integer, String> answers, int sessionId) {
        ExamService.SubmitResult result = EXAM_SERVICE.submitExam(sessionId, answers, false);
        printReport(ctx, result);
    }

    /** 打印成绩单（含每题解析） */
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
                if (r.getAnalysis() != null && !r.getAnalysis().isEmpty()) {
                    System.out.println("    解析：" + r.getAnalysis());
                }
            }
        }
        System.out.println("============================");
    }
}
