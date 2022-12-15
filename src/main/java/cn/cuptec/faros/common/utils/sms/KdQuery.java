package cn.cuptec.faros.common.utils.sms;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Creater: Miao
 * CreateTime: 2018/12/27 10:27
 * Description:
 */
public class KdQuery {

    public static String query(String expCode, String expNo) {
        String host = "https://wuliu.market.alicloudapi.com";
        String path = "/kdi";
        String method = "GET";
        String appcode = "b2b605d5fda04070bdc435dfe804dcd4";  // !!!替换填写自己的AppCode 在买家中心查看
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode); //格式为:Authorization:APPCODE 83359fd73fe11248385f570e3c139xxx
        Map<String, String> querys = new HashMap<String, String>();
        if (!StringUtils.isBlank(expCode))
            querys.put("type", expCode);// !!! 请求参数
        querys.put("no", expNo);// !!! 请求参数
        //JDK 1.8示例代码请在这里下载：  http://code.fegine.com/Tools.zip
        try {
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //System.out.println(response.toString());//如不输出json, 请打开这行代码，打印调试头部状态码。
            //状态码: 200 正常；400 URL无效；401 appCode错误； 403 次数用完； 500 API网管错误
            //获取response的body
            String result = EntityUtils.toString(response.getEntity()); //输出json
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getExpressList(){
        String host = "http://wuliu.market.alicloudapi.com";
        String path = "/getExpressList";
        String method = "GET";
        String appcode = "b2b605d5fda04070bdc435dfe804dcd4";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
//        querys.put("type", "zto");
        try {
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
