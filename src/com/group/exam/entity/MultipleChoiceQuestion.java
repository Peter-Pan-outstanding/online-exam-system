package com.group.exam.entity;

import java.util.List;

/**
 * 多选题：四个选项，答案为一个或多个字母的组合（如 "ABC"）。
 * 判分规则：与正确答案完全一致才得分，多选、少选、错选均不得分。
 */
public class MultipleChoiceQuestion extends Question {

    private List<String> options;

    public MultipleChoiceQuestion() {
        setQuestionType(com.group.exam.common.Constants.TYPE_MULTIPLE);
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String typeName() {
        return "多选题";
    }

    @Override
    public String answerTip() {
        return "请输入答案字母组合（如 ABC，多选无空格）：";
    }

    @Override
    public boolean isValidAnswer(String userAnswer) {
        if (userAnswer == null || !userAnswer.matches("[a-dA-D]{1,4}")) {
            return false;
        }
        // 不允许重复字母
        String upper = userAnswer.toUpperCase();
        return upper.indexOf(upper.charAt(0)) == upper.lastIndexOf(upper.charAt(0));
    }

    @Override
    public String toDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(typeName()).append(']').append(getContent()).append("（多选题，少选不得分）");
        String[] letters = {"A", "B", "C", "D"};
        for (int i = 0; i < 4 && options != null && i < options.size(); i++) {
            sb.append(System.lineSeparator()).append("  ").append(letters[i]).append(". ").append(options.get(i));
        }
        return sb.toString();
    }
}
