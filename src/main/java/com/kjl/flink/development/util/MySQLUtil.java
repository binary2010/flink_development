package com.kjl.flink.development.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Desc: MySQL 工具类
 * Created by zhisheng on 2019-05-24
 * blog：http://www.54tianzhisheng.cn/
 * 微信公众号：zhisheng
 */
public class MySQLUtil {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl("jdbc:oracle:thin:@172.18.12.26:1521/rac");
        config.setUsername("his");
        config.setPassword("his");

//        config.setJdbcUrl( "jdbc:oracle:thin:@172.18.10.140:1521:test140" );
//        config.setUsername( "his" );
//        config.setPassword( "140" );

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection(String driver, String url, String user, String password) {
        Connection con = null;
        try {
            Class.forName(driver);
            //注意，这里替换成你自己的mysql 数据库路径和用户名、密码
            con = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("-----------mysql get connection has exception , msg = " + e.getMessage());
        }
        return con;
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static String converClobToString(Clob clob) {
        if (clob != null) {
            try {
                return clob.getSubString(1, (int) clob.length());
            } catch (SQLException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
