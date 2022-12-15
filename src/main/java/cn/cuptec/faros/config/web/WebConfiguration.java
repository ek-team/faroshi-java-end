package cn.cuptec.faros.config.web;

import cn.cuptec.faros.config.web.httpMessageConverter.OAuth2AccessTokenMessageConverter;
//import cn.cuptec.faros.config.web.httpMessageConverter.RegistrationConverter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
//import de.codecentric.boot.admin.server.domain.values.Registration;
//import de.codecentric.boot.admin.server.utils.jackson.RegistrationDeserializer;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.*;

/**
 * Creater: Miao
 * CreateTime: 2019/4/15 16:18
 * Description:
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, oAuth2AccessTokenAbstractHttpMessageConverter());
//        converters.add(1, egistrationAbstractHttpMessageConverter());
        converters.add(1, fastJsonHttpMessageConverters());
    }

    /**
     * HttpMessageConverters FastJson替换Jackson
     * @return
     */
     public FastJsonHttpMessageConverter fastJsonHttpMessageConverters() {
         FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();


         FastJsonConfig fastJsonConfig = new FastJsonConfig();
         fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        // fastJsonConfig.setCharset(Charset.forName("gb2312"));
         fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
         List<MediaType> fastMediaTypes = new ArrayList<>();
         fastMediaTypes.add(MediaType.APPLICATION_JSON);
//        //解析全部类型
//         fastMediaTypes.add(MediaType.ALL);
         MediaType mediaType = MediaType.valueOf("application/vnd.spring-boot.actuator.v2+json");
         fastMediaTypes.add(mediaType);


         fastConverter.setSupportedMediaTypes(fastMediaTypes);
         fastConverter.setFastJsonConfig(fastJsonConfig);
         return fastConverter;
     }

     public AbstractHttpMessageConverter<OAuth2AccessToken> oAuth2AccessTokenAbstractHttpMessageConverter(){
         return new OAuth2AccessTokenMessageConverter();
     }

//    public AbstractHttpMessageConverter<Registration> egistrationAbstractHttpMessageConverter(){
//        return new RegistrationConverter();
//    }

}


