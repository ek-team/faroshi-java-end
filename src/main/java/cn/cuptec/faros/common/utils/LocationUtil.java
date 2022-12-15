package cn.cuptec.faros.common.utils;

import cn.cuptec.faros.common.bean.AddressComponent;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 位置工具类
 */
public class LocationUtil {

    /**
     * 百度地图api key
     */
    private static String BAIDU_MAP_KEY = "ZFCBZ-YE3C3-HD73E-Y6OP2-OFXGZ-5FBFP";
    /**
     * 百度地图逆地址解析url
     */
    private static String GEOCODER_URL_BAIDU = "https://apis.map.qq.com/ws/geocoder/v1/?location=";

    /**
     * 百度逆地址解析
     * @param lng
     * @param lat
     */
    public static AddressComponent geocoder_baidu(double lng, double lat){
        HttpRequest get = HttpUtil.createGet
                (GEOCODER_URL_BAIDU + lat + CommonConstants.COMMA + lng + "&key=" + BAIDU_MAP_KEY);
        HttpResponse execute = get.execute();
        if (execute.isOk()){
            String body = execute.body();
            JSONObject jsonObject = JSON.parseObject(body);
            JSONObject addressComponent = jsonObject.getJSONObject("result").getJSONObject("address_component");
            return addressComponent.toJavaObject(AddressComponent.class);
        }
        return null;
    }

}
