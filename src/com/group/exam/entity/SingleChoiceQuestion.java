package com.group.exam.entity;

import java.util.List;

/**
 * 单选题：四个选项（A/B/C/D），答案为一个字母。
 */
public class SingleChoiceQuestion extends Question {

    private List<String> options;

    public SingleChoiceQuestion() {
        setQuestionType(com.group.exam.common.Constants.TYPE_SINGLE);
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String typeName() {
        return "单选题";
    }

    @Override
    public String answerTip() {
        return "请输入答案字母（A/B/C/D）：";
    }

    @Override
    public boolean isValidAnswer(String userAnswer) {
        return userAnswer != null && userAnswer.matches("[a-dA-D]");
    }

    @Override
    public String toDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(typeName()).append(']').append(getContent());
        String[] letters = {"A", "B", "C", "D"};
        for (int i = 0; i < 4 && options != null && i < options.size(); i++) {
            sb.append(System.lineSeparator()).append("  ").append(letters[i]).append(". ").append(options.get(i));
        }
        return sb.toString();
    }
}
