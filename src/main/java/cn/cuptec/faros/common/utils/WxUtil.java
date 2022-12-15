package cn.cuptec.faros.common.utils;

public class WxUtil {

    public static String getWxMessageEventKey(String originEventKey){
        if (StringUtils.isNotEmpty(originEventKey) && originEventKey.startsWith("qrscene_")) {
            return originEventKey.replace("qrscene_", "");
        }else{
            return originEventKey;
        }
    }

}
