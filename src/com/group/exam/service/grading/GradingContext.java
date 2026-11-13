package com.group.exam.service.grading;

import com.group.exam.entity.Question;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 判分上下文：按题型注册并调度判分策略（策略模式的外层容器）。
 */
public class GradingContext {

    private final Map<String, GradingStrategy> strategies = new LinkedHashMap<String, GradingStrategy>();

    public GradingContext() {
        register(new SingleChoiceStrategy());
        register(new MultipleChoiceStrategy());
        register(new JudgeStrategy());
    }

    /**
     * 注册一个判分策略。
     */
    public void register(GradingStrategy strategy) {
        strategies.put(strategy.type(), strategy);
    }

    /**
     * 根据题型分发判分；未注册题型按 0 分处理。
     */
    public double grade(Question question, String userAnswer) {
        GradingStrategy strategy = strategies.get(question.getQuestionType());
        if (strategy == null) {
            return 0;
        }
        return strategy.grade(question, userAnswer);
    }
}
