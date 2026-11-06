package com.group.exam.service.grading;

import com.group.exam.entity.Question;

/**
 * 判分策略接口：每种题型对应一种判分策略（策略模式）。
 *
 * <p>策略模式说明：判分算法被封装在独立的策略类中，通过 后续的判分上下文
 * 按题型注册与调度。新增题型时只需新增一个策略类并注册，无需修改现有判分代码，
 * 符合开闭原则。</p>
 */
public interface GradingStrategy {

    /**
     * 支持的题型（SINGLE / MULTIPLE / JUDGE）。
     */
    String type();

    /**
     * 对一道题进行判分。
     *
     * @param question   题目
     * @param userAnswer 学生作答
     * @return 本题得分（0 或满分）
     */
    double grade(Question question, String userAnswer);
}
