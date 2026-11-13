package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.PaperQuestionDao;
import com.group.exam.dao.QuestionDao;
import com.group.exam.entity.Question;

import java.sql.SQLException;
import java.util.List;

/**
 * 题库服务：题目的增删改查与随机抽题。
 */
public class QuestionService {

    private final QuestionDao questionDao = DaoFactory.questionDao();
    private final PaperQuestionDao paperQuestionDao = DaoFactory.paperQuestionDao();

    /**
     * 新增题目。
     */
    public int add(Question question, int createdBy) {
        validate(question);
        try {
            return questionDao.insert(question, createdBy);
        } catch (SQLException e) {
            throw new BizException("新增题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 修改题目。
     */
    public void update(Question question) {
        if (question.getQuestionId() <= 0) {
            throw new BizException("题目编号无效");
        }
        validate(question);
        try {
            questionDao.update(question);
        } catch (SQLException e) {
            throw new BizException("修改题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除题目：被试卷引用时禁止删除，保证数据完整性。
     */
    public void delete(int questionId) {
        try {
            if (paperQuestionDao.countByQuestionId(questionId) > 0) {
                throw new BizException("该题目已被试卷引用，无法删除。如需删除请先移除试卷中的引用");
            }
            questionDao.deleteById(questionId);
        } catch (SQLException e) {
            throw new BizException("删除题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询题目列表。
     *
     * @param typeFilter 题型过滤，null 或空表示全部
     */
    public List<Question> list(String typeFilter) {
        try {
            return questionDao.findAll(typeFilter);
        } catch (SQLException e) {
            throw new BizException("查询题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 按编号查询题目。
     */
    public Question findById(int questionId) {
        try {
            Question question = questionDao.findById(questionId);
            if (question == null) {
                throw new BizException("题目不存在");
            }
            return question;
        } catch (SQLException e) {
            throw new BizException("查询题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 随机抽取指定数量某题型题目；数量不足时抛出业务异常。
     */
    public List<Question> randomByType(String type, int count) {
        if (count <= 0) {
            return new java.util.ArrayList<Question>();
        }
        try {
            List<Question> result = questionDao.findRandomByType(type, count);
            if (result.size() < count) {
                throw new BizException("题库中「" + typeName(type) + "」数量不足（需要 " + count
                        + " 道，现有 " + result.size() + " 道），请先补充题目");
            }
            return result;
        } catch (SQLException e) {
            throw new BizException("随机抽题失败：" + e.getMessage(), e);
        }
    }

    public int countByType(String type) {
        try {
            return questionDao.countByType(type);
        } catch (SQLException e) {
            throw new BizException("统计题目失败：" + e.getMessage(), e);
        }
    }

    private void validate(Question question) {
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BizException("题干不能为空");
        }
        if (question.getScore() <= 0) {
            throw new BizException("分值必须大于 0");
        }
        if (question.getDifficulty() < 1 || question.getDifficulty() > 3) {
            throw new BizException("难度等级只能为 1-3");
        }
        String answer = question.getCorrectAnswer();
        if (answer == null || !question.isValidAnswer(answer)) {
            throw new BizException("答案格式不正确：" + question.answerTip().replace("请输入", "请按格式输入"));
        }
    }

    private String typeName(String type) {
        if (com.group.exam.common.Constants.TYPE_SINGLE.equals(type)) {
            return "单选题";
        }
        if (com.group.exam.common.Constants.TYPE_MULTIPLE.equals(type)) {
            return "多选题";
        }
        if (com.group.exam.common.Constants.TYPE_JUDGE.equals(type)) {
            return "判断题";
        }
        return type;
    }
}
