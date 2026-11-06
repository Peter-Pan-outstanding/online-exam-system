package com.group.exam.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集行映射器：将一行查询结果映射为实体对象。
 *
 * @param <T> 实体类型
 */
public interface RowMapper<T> {

    T map(ResultSet rs) throws SQLException;
}
