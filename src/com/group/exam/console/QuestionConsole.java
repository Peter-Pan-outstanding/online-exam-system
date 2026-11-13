package com.group.exam.console;

import com.group.exam.common.BizException;
import com.group.exam.common.ConsoleKit;
import com.group.exam.common.Constants;
import com.group.exam.entity.Question;
import com.group.exam.entity.QuestionFactory;
import com.group.exam.entity.User;
import com.group.exam.service.QuestionService;

import java.util.ArrayList;
import java.util.List;

/**
 * 题库管理控制台：新增、查看、修改、删除题目。
 */
public final class QuestionConsole {

    private static final QuestionService QUESTION_SERVICE = new QuestionService();

    private QuestionConsole() {
    }

    public static void showMenu(User teacher) {
        while (true) {
            System.out.println("\n---------- 题库管理 ----------");
            System.out.println(" 1. 新增题目");
            System.out.println(" 2. 查看题目");
            System.out.println(" 3. 修改题目");
            System.out.println(" 4. 删除题目");
            System.out.println(" 0. 返回上级");
            int choice = ConsoleKit.readIntRange("请选择：", 0, 4);
            switch (choice) {
                case 1:
                    addQuestion(teacher);
                    break;
                case 2:
                    listQuestions();
                    break;
                case 3:
                    updateQuestion(teacher);
                    break;
                case 4:
                    deleteQuestion();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    /** 新增题目 */
    private static void addQuestion(User teacher) {
        System.out.println("请选择题型：1. 单选题  2. 多选题  3. 判断题");
        int typeChoice = ConsoleKit.readIntRange("请输入：", 1, 3);
        String type = typeChoice == 1 ? Constants.TYPE_SINGLE
                : typeChoice == 2 ? Constants.TYPE_MULTIPLE : Constants.TYPE_JUDGE;

        String content = ConsoleKit.readLine("题干：");
        if (content.isEmpty()) {
            throw new BizException("题干不能为空");
        }
        List<String> options = new ArrayList<String>();
        if (typeChoice == 1 || typeChoice == 2) {
            for (char c = 'A'; c <= 'D'; c++) {
                String opt = ConsoleKit.readLine("选项 " + c + "：");
                if (opt.isEmpty()) {
                    throw new BizException("选项不能为空");
                }
                options.add(opt);
            }
        }
        String answer;
        if (typeChoice == 1) {
            answer = ConsoleKit.readLine("正确答案（A/B/C/D）：").toUpperCase();
        } else if (typeChoice == 2) {
            answer = ConsoleKit.readLine("正确答案（字母组合，如 ABC）：").toUpperCase();
        } else {
            answer = ConsoleKit.readLine("正确答案（T/F）：").toUpperCase();
        }
        double score = ConsoleKit.readInt("分值：");
        int difficulty = ConsoleKit.readIntRange("难度（1-3）：", 1, 3);

        Question question = QuestionFactory.create(type, content, options, answer, score, difficulty);
        int id = QUESTION_SERVICE.add(question, teacher.getUserId());
        System.out.println("新增成功，题目编号：" + id);
    }

    /** 查看题目（可按题型过滤） */
    private static void listQuestions() {
        System.out.println("按题型过滤：1. 全部  2. 单选  3. 多选  4. 判断");
        int filter = ConsoleKit.readIntRange("请输入：", 1, 4);
        String type = filter == 2 ? Constants.TYPE_SINGLE
                : filter == 3 ? Constants.TYPE_MULTIPLE
                : filter == 4 ? Constants.TYPE_JUDGE : null;
        List<Question> list = QUESTION_SERVICE.list(type);
        if (list.isEmpty()) {
            System.out.println("题库为空。");
            return;
        }
        System.out.println("编号  题型      分值  难度  答案  题干");
        for (Question q : list) {
            System.out.printf(" %-5d %-8s %-5.1f %-4d %-4s %s%n",
                    q.getQuestionId(), q.typeName(), q.getScore(), q.getDifficulty(),
                    q.getCorrectAnswer(), q.getContent());
        }
    }

    /** 修改题目：可保留原值（直接回车） */
    private static void updateQuestion(User teacher) {
        int id = ConsoleKit.readInt("请输入要修改的题目编号：");
        Question old = QUESTION_SERVICE.findById(id);
        System.out.println("原题：" + old.toDisplay());
        System.out.println("（以下各字段直接回车表示保留原值）");
        String content = ConsoleKit.readLineOrDefault("题干：", old.getContent());
        List<String> options = new ArrayList<String>();
        if (old instanceof com.group.exam.entity.SingleChoiceQuestion
                || old instanceof com.group.exam.entity.MultipleChoiceQuestion) {
            List<String> oldOptions = old instanceof com.group.exam.entity.SingleChoiceQuestion
                    ? ((com.group.exam.entity.SingleChoiceQuestion) old).getOptions()
                    : ((com.group.exam.entity.MultipleChoiceQuestion) old).getOptions();
            for (int i = 0; i < 4; i++) {
                String opt = ConsoleKit.readLineOrDefault("选项 " + (char) ('A' + i) + "：", oldOptions.get(i));
                options.add(opt);
            }
        }
        String answer = ConsoleKit.readLineOrDefault("正确答案：", old.getCorrectAnswer()).toUpperCase();
        double score = ConsoleKit.readIntOrDefault("分值：", (int) old.getScore());
        int difficulty = ConsoleKit.readIntRangeOrDefault("难度（1-3）：", old.getDifficulty());

        Question question = QuestionFactory.create(old.getQuestionType(), content, options,
                answer, score, difficulty);
        question.setQuestionId(id);
        QUESTION_SERVICE.update(question);
        System.out.println("修改成功。");
    }

    /** 删除题目（被试卷引用时服务层会拒绝） */
    private static void deleteQuestion() {
        int id = ConsoleKit.readInt("请输入要删除的题目编号：");
        String confirm = ConsoleKit.readLine("确认删除该题目？(y/n)：");
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("已取消。");
            return;
        }
        QUESTION_SERVICE.delete(id);
        System.out.println("删除成功。");
    }
}
