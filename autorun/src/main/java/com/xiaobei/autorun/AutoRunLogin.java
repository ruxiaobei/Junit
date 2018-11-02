package com.xiaobei.autorun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaobei.login.Login;
import com.xiaobei.util.ExcelUtils;
import com.xiaobei.util.ExcelUtilsRowMapper;
import com.xiaobei.util.HttpUtils;

@SuppressWarnings("rawtypes")
public class AutoRunLogin {

  public static void main(String[] args) throws Exception {
    ExcelUtils utils = new ExcelUtils();//初始化excel工具
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//		String now = sdf.format(new Date());
    utils.createOneNewExcel("自动化接口测试报告", "登录测试报告");

    // 获取验证码接口
    HttpUtils login = new HttpUtils();//初始化HTTP工具
    String loginUrl = "http://fxtest.wxb.com.cn/common/auth/valiByMobile";//设置接口地址
    Map<String, String> loginQuestJsonParams = Maps.newHashMap();//设置请求参数：key-value形式
    Login login1 = new Login();
    loginQuestJsonParams.put("mobile", "15158136496");
    loginQuestJsonParams.put("type", "login");
    Map oneMap = login.sendPost(loginUrl, loginQuestJsonParams);//发送请求

		/*
         * 添加响应結果
		 */
    List<Object> dataList = new ArrayList<Object>();
    dataList.add(oneMap);
    System.out.println(dataList);
    /*
     * 下面不用动的
		 * 将响应报文填充到excel表格中
		 */
    utils.populateCurrExcel(dataList, new ExcelUtilsRowMapper() {
      public Object[] rowMapping(Object record) throws Exception {
        Map result = (Map) record;
        String url = (String) result.get("url");
        String autoname = "http://fxtest.wxb.com.cn/common/auth/valiByMobile".equals(url) ? "获取验证码" : "error";
        String request = (String) result.get("request");
        Map requestmap = JSON.parseObject(request, Map.class);
        String type = (String) requestmap.get("type");
        String typename = "login".equals(type) ? "登录" : "其他";
        String mobile = (String) requestmap.get("mobile");
        String response = (String) result.get("response");
        Map responseMap = JSON.parseObject(response, Map.class);
        String errorCode = responseMap.get("errorCode").toString();
        String executeResult = "10000".equals(errorCode) ? "pass" : "error";
        Object[] values = {mobile, autoname, typename, executeResult, errorCode};
        return values;
      }
    });
    utils.writeCurrExcelToLocal("d://");
  }
}
