package com.group.exam.dao;

import com.group.exam.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象。
 */
public class UserDao extends BaseDao {

    private static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User map(ResultSet rs) throws SQLException {
            User u = new User();
            u.setUserId(rs.getInt("user_id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setRealName(rs.getString("real_name"));
            u.setRole(rs.getString("role"));
            u.setStudentNo(rs.getString("student_no"));
            u.setCreatedAt(rs.getString("created_at"));
            return u;
        }
    };

    private static final String BASE_COLUMNS =
            "user_id, username, password, real_name, role, student_no, created_at";

    /**
     * 按用户名查询用户。
     */
    public User findByUsername(String username) throws SQLException {
        return queryOne("SELECT " + BASE_COLUMNS + " FROM user WHERE username = ?", MAPPER, username);
    }

    /**
     * 按主键查询用户。
     */
    public User findById(int userId) throws SQLException {
        return queryOne("SELECT " + BASE_COLUMNS + " FROM user WHERE user_id = ?", MAPPER, userId);
    }

    /**
     * 新增用户，返回自增主键。
     */
    public int insert(User user) throws SQLException {
        return insertReturnKey(
                "INSERT INTO user (username, password, real_name, role, student_no) VALUES (?, ?, ?, ?, ?)",
                user.getUsername(), user.getPassword(), user.getRealName(),
                user.getRole(), user.getStudentNo());
    }
}
