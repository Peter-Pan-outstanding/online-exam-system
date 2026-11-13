package com.group.exam.dao;

import com.group.exam.common.Constants;
import com.group.exam.entity.JudgeQuestion;
import com.group.exam.entity.MultipleChoiceQuestion;
import com.group.exam.entity.Question;
import com.group.exam.entity.QuestionFactory;
import com.group.exam.entity.SingleChoiceQuestion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * 题目数据访问对象。
 */
public class QuestionDao extends BaseDao {

    private static final String BASE_COLUMNS =
            "question_id, question_type, content, option_a, option_b, option_c, option_d, "
                    + "correct_answer, score, difficulty, created_by, created_at";

    private static final RowMapper<Question> MAPPER = new RowMapper<Question>() {
        @Override
        public Question map(ResultSet rs) throws SQLException {
            List<String> options = new ArrayList<String>();
            options.add(rs.getString("option_a"));
            options.add(rs.getString("option_b"));
            options.add(rs.getString("option_c"));
            options.add(rs.getString("option_d"));
            Question q = QuestionFactory.create(
                    rs.getString("question_type"),
                    rs.getString("content"),
                    options,
                    rs.getString("correct_answer"),
                    rs.getDouble("score"),
                    rs.getInt("difficulty"));
            q.setQuestionId(rs.getInt("question_id"));
            q.setCreatedBy(rs.getInt("created_by"));
            q.setCreatedAt(rs.getString("created_at"));
            return q;
        }
    };

    /**
     * 新增题目，返回自增主键。
     */
    public int insert(Question q, int createdBy) throws SQLException {
        List<String> options = extractOptions(q);
        return insertReturnKey(
                "INSERT INTO question (question_type, content, option_a, option_b, option_c, option_d, "
                        + "correct_answer, score, difficulty, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                q.getQuestionType(), q.getContent(), options.get(0), options.get(1),
                options.get(2), options.get(3), q.getCorrectAnswer(),
                q.getScore(), q.getDifficulty(), createdBy);
    }

    /**
     * 更新题目。
     */
    public int update(Question q) throws SQLException {
        List<String> options = extractOptions(q);
        return update(
                "UPDATE question SET content = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, "
                        + "correct_answer = ?, score = ?, difficulty = ? WHERE question_id = ?",
                q.getContent(), options.get(0), options.get(1), options.get(2), options.get(3),
                q.getCorrectAnswer(), q.getScore(), q.getDifficulty(), q.getQuestionId());
    }

    /**
     * 删除题目。
     */
    public int deleteById(int questionId) throws SQLException {
        return update("DELETE FROM question WHERE question_id = ?", questionId);
    }

    public Question findById(int questionId) throws SQLException {
        return queryOne("SELECT " + BASE_COLUMNS + " FROM question WHERE question_id = ?",
                MAPPER, questionId);
    }

    /**
     * 按 ID 集合批量查询（用于组卷后按 ID 取题）。
     */
    public List<Question> findByIds(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<Question>();
        }
        StringJoiner placeholders = new StringJoiner(",", "(", ")");
        for (Integer id : ids) {
            placeholders.add("?");
        }
        String sql = "SELECT " + BASE_COLUMNS + " FROM question WHERE question_id IN " + placeholders.toString();
        return queryList(sql, MAPPER, ids.toArray());
    }

    /**
     * 查询题目列表；typeFilter 为空表示全部。
     */
    public List<Question> findAll(String typeFilter) throws SQLException {
        if (typeFilter == null || typeFilter.trim().isEmpty()) {
            return queryList("SELECT " + BASE_COLUMNS + " FROM question ORDER BY question_id", MAPPER);
        }
        return queryList("SELECT " + BASE_COLUMNS + " FROM question WHERE question_type = ? ORDER BY question_id",
                MAPPER, typeFilter.trim().toUpperCase());
    }

    /**
     * 统计某题型题目数量。
     */
    public int countByType(String type) throws SQLException {
        return queryOne("SELECT COUNT(*) AS c FROM question WHERE question_type = ?",
                new RowMapper<Integer>() {
                    @Override
                    public Integer map(ResultSet rs) throws SQLException {
                        return rs.getInt("c");
                    }
                }, type);
    }

    /**
     * 按题型随机抽取 count 道题（数据库端随机排序）。
     */
    public List<Question> findRandomByType(String type, int count) throws SQLException {
        return queryList(
                "SELECT " + BASE_COLUMNS + " FROM question WHERE question_type = ? ORDER BY RAND() LIMIT ?",
                MAPPER, type, count);
    }

    /** 取出题目对象中的四个选项（判断题返回空列表） */
    private List<String> extractOptions(Question q) {
        List<String> options = new ArrayList<String>();
        if (q instanceof SingleChoiceQuestion) {
            options.addAll(((SingleChoiceQuestion) q).getOptions());
        } else if (q instanceof MultipleChoiceQuestion) {
            options.addAll(((MultipleChoiceQuestion) q).getOptions());
        }
        while (options.size() < 4) {
            options.add(null);
        }
        return options;
    }
}
