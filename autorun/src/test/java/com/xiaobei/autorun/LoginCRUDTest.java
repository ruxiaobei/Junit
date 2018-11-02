package com.xiaobei.autorun;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaobei.util.ExcelUtils;
import com.xiaobei.util.ExcelUtilsRowMapper;
import com.xiaobei.util.HttpUtils;
import com.xiaobei.util.JdbcUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author
 */
public class LoginCRUDTest {

  private static Connection conn = null;
  private static Statement st = null;
  private static ResultSet rs = null;

  @BeforeClass
  public static void beforeClass() throws SQLException {
    System.out.println("初始化数据库连接");
    conn = JdbcUtils.getConnection();
    st = conn.createStatement();
  }

  @AfterClass
  public static void afterClass() throws SQLException {
    System.out.println("释放数据库连接");
    JdbcUtils.free(rs, st, conn);
  }

  /**
   * 插入一条数据
   */
  @Test
  public void testAdd() throws Exception {
    String sql = "insert into user VALUES(7,\"小北1\",\"cccccccc\")";
    try {
      st.executeUpdate(sql);
      System.out.println("[" + sql + "]数据插入success!!!");
    } catch (Exception e) {
      System.out.println("[" + sql + "]数据插入failure!!!, 原因：" + e.getMessage());
    }
  }

  /**
   * 修改一条数据
   */
  @Test
  public void testUpdate() throws Exception {
    String sql = "update user set mobile = CONCAT(mobile,'10')   where id = 7";
    try {
      st.executeUpdate(sql);
      System.out.println("[" + sql + "]数据修改success!!!");
    } catch (Exception e) {
      System.out.println("[" + sql + "]数据修改failure!!!, 原因：" + e.getMessage());
    }
  }

  /**
   * 删除一条数据
   */
  @Test
  public void testDelete() throws Exception {
    String sql = "delete from user where id > 6";
    try {
      st.executeUpdate(sql);
      System.out.println("[" + sql + "]数据删除success!!!");
    } catch (Exception e) {
      System.out.println("[" + sql + "]数据删除failure!!!, 原因：" + e.getMessage());
    }
  }

  /**
   * 查询记录
   */
  @Test
  public void testFind() throws Exception {
    String sql = "select id, username, mobile  from user";
    int total = 0;
    try {
      rs = st.executeQuery(sql);
      System.out.println("查询结果：");
      while (rs.next()) {
        total++;
        System.out.println(rs.getObject("id") + "\t"
                + rs.getObject("username") + "\t"
                + rs.getObject("mobile"));
      }
      System.out.println("总计：[" + total + "]条");
    } catch (Exception e) {
      System.out.println("[" + sql + "]数据查询failure!!!, 原因：" + e.getMessage());
    }
  }

  @Test

  public void testLogin() throws Exception {
    // 查询user表
    String sql = "select id, jsonData  from pay";
    Object[] results = queryUserTable(sql);

    // 向接口地址发送post请求
    if (ArrayUtils.isNotEmpty(results)) {
      List<Integer> idsList = (List<Integer>) results[0];
      Map<Integer, Map<String, String>> recordsMap = (Map<Integer, Map<String, String>>) results[1];
      String url = "http://218.244.135.30/insurance/callback/epicc/pay";
      List<Object> resultList = postToLoginModule(url, idsList, recordsMap);
      // 处理响应报文:生成excel
      generateExcel(resultList);
    }
  }

  /**
   * 生成excel
   *
   * @param resultList
   */
  private void generateExcel(List<Object> resultList) throws Exception {
    ExcelUtils utils = new ExcelUtils();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    String now = sdf.format(new Date());
    utils.createOneNewExcel("自动化接口测试报告" + now, "测试结果");//重新生成一份報告
    utils.populateCurrExcel(resultList, new ExcelUtilsRowMapper() {
      public Object[] rowMapping(Object record) throws Exception {
        Map result = (Map) record;
        String url = (String) result.get("url");
        String response = (String) result.get("response");
        Map responseMap = JSON.parseObject(response, Map.class);
        // Map responseHeaderMap =JSON.parseObject(responseMap.get("header").toString(),Map.class);
        String errorCode = responseMap.get("errorCode").toString();
        String executeResult = "10000".equals(errorCode) ? "pass" : "error";
        String errorMessage = responseMap.get("errorMsg").toString();
        Object[] values = {url, "-", "-", executeResult, errorCode, errorMessage, response};
        return values;
      }
    });
    utils.writeCurrExcelToLocal("d://");//指定測試報告存放目錄
  }

  /**
   * 向login模块发送请求
   *
   * @param idsList
   * @param recordsMap
   * @return
   */
  private List<Object> postToLoginModule(String url, List<Integer> idsList, Map<Integer, Map<String, String>> recordsMap) throws Exception {
    List<Object> resultList = Lists.newArrayList();
    HttpUtils httpUtils = new HttpUtils();
    for (Integer id : idsList) {
      Map<String, String> currRecordEntry = recordsMap.get(id);
      System.out.println("正在发送第：[" + id + "]个用例请求，请求参数为：" + currRecordEntry);
      Map resultMap = httpUtils.sendPost(url, currRecordEntry);
      resultList.add(resultMap);
    }
    return resultList;
  }

  /**
   * 查询user表
   *
   * @param sql 查询语句
   * @return 返回user.id列表
   */
  private Object[] queryUserTable(String sql) throws Exception {
    int total = 0;
    Object[] results = new Object[2];
    try {
      List<Integer> idsList = Lists.newArrayList();
      Map<Integer, Map<String, String>> recordsMap = Maps.newHashMap();
      rs = st.executeQuery(sql);
      System.out.println("查询结果：");
      while (rs.next()) {
        total++;
        Integer everyId = Integer.valueOf(rs.getObject("id").toString());
        idsList.add(everyId);
        Map<String, String> tempMap = Maps.newHashMap();
        tempMap.put("jsonData", rs.getObject("jsonData").toString());
//                tempMap.put("mobile",  rs.getObject("mobile").toString());
        recordsMap.put(everyId, tempMap);
      }
      results[0] = idsList;
      results[1] = recordsMap;
      System.out.println("总计：[" + total + "]条");
    } catch (Exception e) {
      System.out.println("[" + sql + "]数据查询failure!!!, 原因：" + e.getMessage());
      throw e;
    }
    return results;
  }
}
