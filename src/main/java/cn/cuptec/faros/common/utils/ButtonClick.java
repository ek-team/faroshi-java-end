package cn.cuptec.faros.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @Description: 百度 反地理编译 获取所在地区
 * @Author mby
 * @Date 2020/7/31 14:10
 */
public class ButtonClick {
    private static final String BAIDU_APP_KEY = "HYLWVxp1GewzLVLBiabXKNTgQSnf3NUM";
    public static String changeCity(Double lng,Double lat) {
        String res;
     /*   Double lng=120.271000;
        Double lat=30.400100;*/
        try {
            URL resjson = new URL("http://api.map.baidu.com/reverse_geocoding/v3/?ak=" + BAIDU_APP_KEY + "&output=json&coordtype=wgs84ll&location=" + lat + "," + lng);
            BufferedReader in = new BufferedReader(new InputStreamReader(resjson.openStream()));
            StringBuilder sb = new StringBuilder();
            while ((res = in.readLine()) != null) {
                sb.append(res.trim());
            }
            in.close();
            String str = sb.toString();
/*
            com.alibaba.fastjson.JSONObject jsonObj = com.alibaba.fastjson.JSONObject.parseObject(str);
            Object o = jsonObj.getJSONObject("result").getJSONObject("addressComponent").get("adcode");*/
            return str;
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }
}
