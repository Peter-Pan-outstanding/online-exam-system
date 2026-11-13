package com.group.exam.entity;

/**
 * 试卷题目条目：组合 试卷-题目关联 + 题目详情，供考试与试卷详情展示使用。
 */
public class PaperItem {

    private PaperQuestion paperQuestion;
    private Question question;

    public PaperItem(PaperQuestion paperQuestion, Question question) {
        this.paperQuestion = paperQuestion;
        this.question = question;
    }

    public PaperQuestion getPaperQuestion() {
        return paperQuestion;
    }

    public Question getQuestion() {
        return question;
    }
}
