package com.group.exam.dao;

import com.group.exam.common.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO 基类：封装 JDBC 常用操作（查询、更新、插入返回主键）。
 * 所有连接通过 try-with-resources 自动关闭，避免资源泄漏。
 */
public abstract class BaseDao {

    /**
     * 查询多行。
     */
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = new ArrayList<T>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询单行，无结果返回 null。
     */
    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 执行更新（INSERT / UPDATE / DELETE）。
     *
     * @return 受影响行数
     */
    protected int update(String sql, Object... params) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * 执行插入并返回自增主键。
     */
    protected int insertReturnKey(String sql, Object... params) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("未能获取自增主键");
        }
    }

    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}
