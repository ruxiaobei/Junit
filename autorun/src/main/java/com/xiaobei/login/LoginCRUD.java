package com.xiaobei.login;


import com.xiaobei.util.JdbcUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 基于mysql的增删改查
 *
 * @author Administrator
 */
public class LoginCRUD {

  public static void delete() throws SQLException { //D
    Connection conn = null;
    Statement st = null;
    ResultSet rs = null;
    try {
      conn = JdbcUtils.getConnection();
      st = conn.createStatement();
      String sql = "delete from user where id>4";
      int i = st.executeUpdate(sql);
      System.out.println("i=" + i);
    } finally {
      JdbcUtils.free(rs, st, conn);
    }
  }

  public static void update() throws SQLException {//U
    Connection conn = null;
    Statement st = null;
    ResultSet rs = null;
    try {
      conn = JdbcUtils.getConnection();
      st = conn.createStatement();
      String sql = "update user set money = money + 10 ";
      int i = st.executeUpdate(sql);
      System.out.println("i=" + i);
    } finally {
      JdbcUtils.free(rs, st, conn);
    }
  }

  public static void create() throws SQLException {//C
    Connection conn = null;
    Statement st = null;
    ResultSet rs = null;
    try {
      conn = JdbcUtils.getConnection();
      st = conn.createStatement();
      String sql = "insert into user(name,birthday, money) values ('name1', '1987-01-01', 400) ";
      int i = st.executeUpdate(sql);
      System.out.println("i=" + i);
    } finally {
      JdbcUtils.free(rs, st, conn);
    }
  }

  public static void read() throws SQLException {//R
    Connection conn = null;
    Statement st = null;
    ResultSet rs = null;
    try {
//			conn = JdbcUtils.getConnection();
//			conn = JdbcUtils_ConnectionPool.getConnection();
      conn = JdbcUtils.getConnection();
      st = conn.createStatement();
      rs = st.executeQuery("select id, name, money, birthday  from user");
      while (rs.next()) {
        System.out.println(rs.getObject("id") + "\t"
                + rs.getObject("name") + "\t"
                + rs.getObject("birthday") + "\t"
                + rs.getObject("money"));
      }
    } finally {
      JdbcUtils.free(rs, st, conn);
    }
  }
}
