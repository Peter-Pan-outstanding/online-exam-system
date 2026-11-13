package com.group.exam.service.grading;

import com.group.exam.common.Constants;
import com.group.exam.entity.Question;

/**
 * 单选题判分策略：答案与正确答案完全一致得分。
 */
public class SingleChoiceStrategy implements GradingStrategy {

    @Override
    public String type() {
        return Constants.TYPE_SINGLE;
    }

    @Override
    public double grade(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }
        return userAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer())
                ? question.getScore() : 0;
    }
}
