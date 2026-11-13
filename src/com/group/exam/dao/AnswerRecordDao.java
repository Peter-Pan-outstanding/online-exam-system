package com.group.exam.dao;

import com.group.exam.entity.AnswerRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 答题明细数据访问对象。
 */
public class AnswerRecordDao extends BaseDao {

    private static final String BASE_COLUMNS =
            "record_id, session_id, question_id, user_answer, is_correct, score";

    private static final RowMapper<AnswerRecord> MAPPER = new RowMapper<AnswerRecord>() {
        @Override
        public AnswerRecord map(ResultSet rs) throws SQLException {
            AnswerRecord r = new AnswerRecord();
            r.setRecordId(rs.getInt("record_id"));
            r.setSessionId(rs.getInt("session_id"));
            r.setQuestionId(rs.getInt("question_id"));
            r.setUserAnswer(rs.getString("user_answer"));
            r.setCorrect(rs.getInt("is_correct"));
            r.setScore(rs.getDouble("score"));
            r.setQuestionContent(rs.getString("question_content"));
            r.setCorrectAnswer(rs.getString("correct_answer"));
            return r;
        }
    };

    /**
     * 批量插入答题明细。
     */
    public int[] batchInsert(List<AnswerRecord> records) throws SQLException {
        List<Object[]> params = new ArrayList<Object[]>();
        for (AnswerRecord r : records) {
            params.add(new Object[]{
                    r.getSessionId(), r.getQuestionId(), r.getUserAnswer(),
                    r.getCorrect(), r.getScore()
            });
        }
        return batchUpdate(
                "INSERT INTO answer_record (session_id, question_id, user_answer, is_correct, score) "
                        + "VALUES (?, ?, ?, ?, ?)",
                params);
    }

    /**
     * 查询某次考试的全部答题明细（联表带出题干与正确答案，供成绩单展示）。
     */
    public List<AnswerRecord> findBySessionId(int sessionId) throws SQLException {
        return queryList(
                "SELECT r." + BASE_COLUMNS.replaceAll(", ", ", r.") + ", q.content AS question_content, "
                        + "q.correct_answer AS correct_answer "
                        + "FROM answer_record r "
                        + "JOIN question q ON r.question_id = q.question_id "
                        + "WHERE r.session_id = ? ORDER BY r.record_id",
                MAPPER, sessionId);
    }
}
