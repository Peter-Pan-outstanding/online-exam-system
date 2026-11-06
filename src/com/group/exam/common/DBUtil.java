package com.group.exam.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具：读取 db.properties 配置并创建 JDBC 连接。
 */
public final class DBUtil {

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream("db.properties")) {
                props.load(in);
            }
            String driver = props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
            url = props.getProperty("jdbc.url");
            user = props.getProperty("jdbc.user");
            password = props.getProperty("jdbc.password");
            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "数据库配置加载失败：请在项目根目录放置 db.properties（见 README）。原因：" + e.getMessage());
        }
    }

    private DBUtil() {
    }

    /**
     * 获取一个新的数据库连接。
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
