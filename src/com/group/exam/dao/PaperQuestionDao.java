package com.group.exam.dao;

import com.group.exam.entity.PaperQuestion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 试卷题目关联数据访问对象。
 */
public class PaperQuestionDao extends BaseDao {

    private static final String BASE_COLUMNS = "id, paper_id, question_id, question_order, score";

    private static final RowMapper<PaperQuestion> MAPPER = new RowMapper<PaperQuestion>() {
        @Override
        public PaperQuestion map(ResultSet rs) throws SQLException {
            PaperQuestion pq = new PaperQuestion();
            pq.setId(rs.getInt("id"));
            pq.setPaperId(rs.getInt("paper_id"));
            pq.setQuestionId(rs.getInt("question_id"));
            pq.setQuestionOrder(rs.getInt("question_order"));
            pq.setScore(rs.getDouble("score"));
            return pq;
        }
    };

    /**
     * 按试卷查询全部题目关联（按题序排列）。
     */
    public List<PaperQuestion> findByPaperId(int paperId) throws SQLException {
        return queryList(
                "SELECT " + BASE_COLUMNS + " FROM paper_question WHERE paper_id = ? ORDER BY question_order",
                MAPPER, paperId);
    }

    /**
     * 统计某题目被多少张试卷引用。
     */
    public int countByQuestionId(int questionId) throws SQLException {
        return queryOne("SELECT COUNT(*) AS c FROM paper_question WHERE question_id = ?",
                new RowMapper<Integer>() {
                    @Override
                    public Integer map(ResultSet rs) throws SQLException {
                        return rs.getInt("c");
                    }
                }, questionId);
    }

    /**
     * 统计某试卷包含的题目数。
     */
    public int countByPaperId(int paperId) throws SQLException {
        return queryOne("SELECT COUNT(*) AS c FROM paper_question WHERE paper_id = ?",
                new RowMapper<Integer>() {
                    @Override
                    public Integer map(ResultSet rs) throws SQLException {
                        return rs.getInt("c");
                    }
                }, paperId);
    }
}
