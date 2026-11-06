package com.group.exam.entity;

/**
 * 题目抽象基类：定义题目的公共属性与抽象行为。
 * 三种具体题型（单选 / 多选 / 判断）继承本类，体现继承与多态。
 * 判分逻辑通过策略模式（service.grading 包）实现，本类只负责数据与展示。
 */
public abstract class Question {

    private int questionId;
    private String questionType;
    private String content;
    private String correctAnswer;
    private double score;
    private int difficulty;
    private int createdBy;
    private String createdAt;

    public Question() {
    }

    /** 题型中文名称（用于界面展示） */
    public abstract String typeName();

    /** 作答提示语 */
    public abstract String answerTip();

    /**
     * 校验一个作答答案的格式是否合法。
     *
     * @param userAnswer 学生输入的答案
     * @return true 表示格式合法
     */
    public abstract boolean isValidAnswer(String userAnswer);

    /** 控制台展示：题干 + 选项 */
    public abstract String toDisplay();

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
