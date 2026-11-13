package com.group.exam.dao;

import com.group.exam.common.DBUtil;
import com.group.exam.entity.ExamPaper;
import com.group.exam.entity.PaperQuestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 试卷数据访问对象。
 * 组卷（试卷 + 题目明细）通过事务一次性写入，保证数据一致性。
 */
public class PaperDao extends BaseDao {

    private static final String BASE_COLUMNS =
            "paper_id, paper_name, subject, total_score, duration_minutes, status, created_by, created_at";

    private static final RowMapper<ExamPaper> MAPPER = new RowMapper<ExamPaper>() {
        @Override
        public ExamPaper map(ResultSet rs) throws SQLException {
            ExamPaper p = new ExamPaper();
            p.setPaperId(rs.getInt("paper_id"));
            p.setPaperName(rs.getString("paper_name"));
            p.setSubject(rs.getString("subject"));
            p.setTotalScore(rs.getDouble("total_score"));
            p.setDurationMinutes(rs.getInt("duration_minutes"));
            p.setStatus(rs.getString("status"));
            p.setCreatedBy(rs.getInt("created_by"));
            p.setCreatedAt(rs.getString("created_at"));
            return p;
        }
    };

    /**
     * 新增草稿试卷，返回自增主键。
     */
    public int insert(ExamPaper paper) throws SQLException {
        return insertReturnKey(
                "INSERT INTO exam_paper (paper_name, subject, duration_minutes, created_by) "
                        + "VALUES (?, ?, ?, ?)",
                paper.getPaperName(), paper.getSubject(), paper.getDurationMinutes(), paper.getCreatedBy());
    }

    /**
     * 更新试卷状态（发布 / 下线）。
     */
    public int updateStatus(int paperId, String status) throws SQLException {
        return update("UPDATE exam_paper SET status = ? WHERE paper_id = ?", status, paperId);
    }

    public ExamPaper findById(int paperId) throws SQLException {
        return queryOne("SELECT " + BASE_COLUMNS + " FROM exam_paper WHERE paper_id = ?", MAPPER, paperId);
    }

    /**
     * 查询试卷列表；statusFilter 为空表示全部。
     */
    public List<ExamPaper> findAll(String statusFilter) throws SQLException {
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            return queryList("SELECT " + BASE_COLUMNS + " FROM exam_paper ORDER BY paper_id DESC", MAPPER);
        }
        return queryList("SELECT " + BASE_COLUMNS + " FROM exam_paper WHERE status = ? ORDER BY paper_id DESC",
                MAPPER, statusFilter.trim().toUpperCase());
    }

    /**
     * 事务化组卷：对已存在的试卷更新总分，并写入全部题目明细，任一步失败整体回滚。
     *
     * @param paperId   试卷主键（须为已创建、草稿状态的试卷）
     * @param totalScore 组卷后的总分
     * @param questions  题目明细
     */
    public void attachQuestionsAndSetTotal(int paperId, double totalScore, List<PaperQuestion> questions)
            throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE exam_paper SET total_score = ? WHERE paper_id = ?")) {
                    ps.setDouble(1, totalScore);
                    ps.setInt(2, paperId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO paper_question (paper_id, question_id, question_order, score) "
                                + "VALUES (?, ?, ?, ?)")) {
                    for (PaperQuestion pq : questions) {
                        ps.setInt(1, paperId);
                        ps.setInt(2, pq.getQuestionId());
                        ps.setInt(3, pq.getQuestionOrder());
                        ps.setDouble(4, pq.getScore());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
