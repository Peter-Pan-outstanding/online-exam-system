package com.group.exam.dao;

import com.group.exam.common.Constants;
import com.group.exam.entity.ExamSession;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 考试会话数据访问对象。
 */
public class ExamSessionDao extends BaseDao {

    private static final String BASE_COLUMNS =
            "session_id, paper_id, student_id, start_time, end_time, submit_time, status, total_score, passed";

    private static final RowMapper<ExamSession> MAPPER = new RowMapper<ExamSession>() {
        @Override
        public ExamSession map(ResultSet rs) throws SQLException {
            ExamSession s = new ExamSession();
            s.setSessionId(rs.getInt("session_id"));
            s.setPaperId(rs.getInt("paper_id"));
            s.setStudentId(rs.getInt("student_id"));
            s.setStartTime(rs.getString("start_time"));
            s.setEndTime(rs.getString("end_time"));
            s.setSubmitTime(rs.getString("submit_time"));
            s.setStatus(rs.getString("status"));
            s.setTotalScore(rs.getDouble("total_score"));
            s.setPassed(rs.getInt("passed"));
            return s;
        }
    };

    private static final RowMapper<ExamSession> REPORT_MAPPER = rs -> {
        ExamSession session = MAPPER.map(rs);
        session.setStudentName(rs.getString("student_name"));
        session.setPaperName(rs.getString("paper_name"));
        return session;
    };

    /**
     * 新增考试会话，返回自增主键。
     */
    public int insert(ExamSession session) throws SQLException {
        return insertReturnKey(
                "INSERT INTO exam_session (paper_id, student_id, start_time, end_time, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                session.getPaperId(), session.getStudentId(),
                session.getStartTime(), session.getEndTime(), session.getStatus());
    }

    public ExamSession findById(int sessionId) throws SQLException {
        return queryOne("SELECT " + BASE_COLUMNS + " FROM exam_session WHERE session_id = ?", MAPPER, sessionId);
    }

    /**
     * 查询某学生某试卷尚未完成的会话。
     */
    public ExamSession findActiveByStudentAndPaper(int studentId, int paperId) throws SQLException {
        return queryOne(
                "SELECT " + BASE_COLUMNS + " FROM exam_session "
                        + "WHERE student_id = ? AND paper_id = ? AND status = ? ORDER BY session_id DESC LIMIT 1",
                MAPPER, studentId, paperId, Constants.SESSION_IN_PROGRESS);
    }

    /**
     * 查询某学生全部考试记录（联表带出姓名与试卷名，供学生成绩列表展示）。
     */
    public List<ExamSession> findByStudentId(int studentId) throws SQLException {
        return queryList(
                "SELECT s." + BASE_COLUMNS.replaceAll(", ", ", s.") + ", u.real_name AS student_name, "
                        + "p.paper_name AS paper_name "
                        + "FROM exam_session s "
                        + "JOIN user u ON s.student_id = u.user_id "
                        + "JOIN exam_paper p ON s.paper_id = p.paper_id "
                        + "WHERE s.student_id = ? ORDER BY s.start_time DESC",
                REPORT_MAPPER, studentId);
    }

    /**
     * 查询所有已结束的考试记录（联表带出姓名与试卷名，供教师成绩统计展示）。
     */
    public List<ExamSession> findAllFinished() throws SQLException {
        return queryList(
                "SELECT s." + BASE_COLUMNS.replaceAll(", ", ", s.") + ", u.real_name AS student_name, "
                        + "p.paper_name AS paper_name "
                        + "FROM exam_session s "
                        + "JOIN user u ON s.student_id = u.user_id "
                        + "JOIN exam_paper p ON s.paper_id = p.paper_id "
                        + "WHERE s.status IN ('SUBMITTED', 'TIMEOUT') ORDER BY s.submit_time DESC",
                REPORT_MAPPER);
    }

    /**
     * 交卷后更新成绩与状态。
     */
    public int updateSubmitResult(int sessionId, String status, double totalScore, int passed, String submitTime)
            throws SQLException {
        return update(
                "UPDATE exam_session SET status = ?, total_score = ?, passed = ?, submit_time = ? "
                        + "WHERE session_id = ?",
                status, totalScore, passed, submitTime, sessionId);
    }
}
