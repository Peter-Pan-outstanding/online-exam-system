package com.group.exam.service.grading;

import com.group.exam.common.Constants;
import com.group.exam.entity.Question;

import java.util.Arrays;

/**
 * 多选题判分策略：答案与正确答案完全一致（多选、少选、错选均不得分）。
 * 比较前将字母组合标准化为排序后的小写字符串，避免顺序影响。
 */
public class MultipleChoiceStrategy implements GradingStrategy {

    @Override
    public String type() {
        return Constants.TYPE_MULTIPLE;
    }

    @Override
    public double grade(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }
        return normalize(userAnswer).equals(normalize(question.getCorrectAnswer()))
                ? question.getScore() : 0;
    }

    private String normalize(String answer) {
        String letters = answer.toUpperCase().replaceAll("[^A-D]", "");
        char[] chars = letters.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}
