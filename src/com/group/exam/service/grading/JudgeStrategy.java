package com.group.exam.service.grading;

import com.group.exam.common.Constants;
import com.group.exam.entity.Question;

/**
 * 判断题判分策略：T / F 与正确答案一致得分（忽略大小写）。
 */
public class JudgeStrategy implements GradingStrategy {

    @Override
    public String type() {
        return Constants.TYPE_JUDGE;
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
