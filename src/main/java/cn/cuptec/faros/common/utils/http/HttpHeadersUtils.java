package cn.cuptec.faros.common.utils.http;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.springframework.http.HttpHeaders;

import java.nio.charset.Charset;

/**
 * Creater: Miao
 * CreateTime: 2019/5/31 16:39
 * Description: HttpHeaders工具类
 */
public class HttpHeadersUtils {

    /**
     * 根据用户名、密码创建Basic Auth 的请求头
     * @param username
     * @param password
     * @return
     */
    public static HttpHeaders createBasicAuthHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

}
