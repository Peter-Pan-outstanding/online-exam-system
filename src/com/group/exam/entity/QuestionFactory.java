package com.group.exam.entity;

import com.group.exam.common.BizException;
import com.group.exam.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目工厂：根据题型创建对应的题目子类对象（简单工厂模式）。
 * 由 DAO 层在从数据库读取题目时统一调用，屏蔽子类实例化细节。
 */
public final class QuestionFactory {

    private QuestionFactory() {
    }

    /**
     * 根据题型创建题目对象。
     *
     * @param type          题型（SINGLE / MULTIPLE / JUDGE）
     * @param content       题干
     * @param options       选项列表（判断题为 null）
     * @param correctAnswer 正确答案
     * @param score         分值
     * @param difficulty    难度 1-3
     * @return 对应题型的具体子类对象
     */
    public static Question create(String type, String content, List<String> options,
                                  String correctAnswer, double score, int difficulty) {
        Question question;
        if (Constants.TYPE_SINGLE.equals(type)) {
            SingleChoiceQuestion single = new SingleChoiceQuestion();
            single.setOptions(normalizeOptions(options));
            question = single;
        } else if (Constants.TYPE_MULTIPLE.equals(type)) {
            MultipleChoiceQuestion multiple = new MultipleChoiceQuestion();
            multiple.setOptions(normalizeOptions(options));
            question = multiple;
        } else if (Constants.TYPE_JUDGE.equals(type)) {
            question = new JudgeQuestion();
        } else {
            throw new BizException("未知题型：" + type);
        }
        question.setQuestionType(type);
        question.setContent(content);
        question.setCorrectAnswer(correctAnswer == null ? "" : correctAnswer.trim().toUpperCase());
        question.setScore(score);
        question.setDifficulty(difficulty);
        return question;
    }

    /** 将选项补齐为 4 个，缺项用空串占位 */
    private static List<String> normalizeOptions(List<String> options) {
        List<String> result = new ArrayList<String>();
        if (options != null) {
            for (int i = 0; i < options.size() && i < 4; i++) {
                result.add(options.get(i) == null ? "" : options.get(i));
            }
        }
        while (result.size() < 4) {
            result.add("");
        }
        return result;
    }
}
