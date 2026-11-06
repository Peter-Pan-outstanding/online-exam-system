package com.group.exam.entity;

/**
 * 判断题：答案只有 T（正确）/ F（错误）。
 */
public class JudgeQuestion extends Question {

    public JudgeQuestion() {
        setQuestionType(com.group.exam.common.Constants.TYPE_JUDGE);
    }

    @Override
    public String typeName() {
        return "判断题";
    }

    @Override
    public String answerTip() {
        return "请输入 T（正确）或 F（错误）：";
    }

    @Override
    public boolean isValidAnswer(String userAnswer) {
        return userAnswer != null
                && (userAnswer.equalsIgnoreCase("T") || userAnswer.equalsIgnoreCase("F"));
    }

    @Override
    public String toDisplay() {
        return "[" + typeName() + "] " + getContent() + "（回答 T 表示正确，F 表示错误）";
    }
}
