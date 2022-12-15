package cn.cuptec.faros.config.web.httpMessageConverter;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.SecurityConstants;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.oauth2.common.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OAuth2AccessTokenMessageConverter extends AbstractHttpMessageConverter<OAuth2AccessToken> {

    public static final String EXPIRED = "expired";

    private final FastJsonHttpMessageConverter delegateMessageConverter;

    public OAuth2AccessTokenMessageConverter() {
        super(MediaType.APPLICATION_JSON);
        this.delegateMessageConverter = new FastJsonHttpMessageConverter();
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return OAuth2AccessToken.class.isAssignableFrom(clazz);
    }

    @Override
    protected OAuth2AccessToken readInternal(Class<? extends OAuth2AccessToken> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException(
                "This converter is only used for converting from externally aqcuired form data");
    }

    @Override
    protected void writeInternal(OAuth2AccessToken accessToken, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        Map<String, Object> data = new HashMap<>(8);
        data.put(SecurityConstants.DETAILS_USER_ID, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_USER_ID));
        data.put(SecurityConstants.DETAILS_USERNAME, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_USERNAME));
        data.put(SecurityConstants.DETAILS_DEPT_ID, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_DEPT_ID));
        data.put(SecurityConstants.DETAILS_NICKNAME, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_NICKNAME));
        data.put(SecurityConstants.DETAILS_AVATAR, accessToken.getAdditionalInformation().get(SecurityConstants.DETAILS_AVATAR));
        data.put(SecurityConstants.DETAILS_LICENSE, SecurityConstants.AUTHOR);

        data.put(OAuth2AccessToken.ACCESS_TOKEN, accessToken.getValue());
        data.put(OAuth2AccessToken.TOKEN_TYPE, accessToken.getTokenType());
        if (accessToken instanceof DefaultOAuth2AccessToken){
            data.put(EXPIRED, ((DefaultOAuth2AccessToken)accessToken).isExpired());
        }

        data.put(OAuth2AccessToken.EXPIRES_IN, accessToken.getExpiresIn());
        data.put(OAuth2AccessToken.SCOPE,accessToken.getScope());
        if (accessToken.getRefreshToken() != null){
            data.put(OAuth2AccessToken.REFRESH_TOKEN, accessToken.getRefreshToken().getValue());
            if (accessToken.getRefreshToken().getClass().isAssignableFrom(ExpiringOAuth2RefreshToken.class)){
                data.put(OAuth2AccessToken.REFRESH_TOKEN + StringPool.UNDERSCORE + OAuth2AccessToken.EXPIRES_IN, ((ExpiringOAuth2RefreshToken)accessToken.getRefreshToken()).getExpiration());
            }
        }

        OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
        if (Objects.nonNull(refreshToken)) {
            data.put(OAuth2AccessToken.REFRESH_TOKEN, refreshToken.getValue());
        }
        delegateMessageConverter.write(RestResponse.ok(data), MediaType.APPLICATION_JSON, outputMessage);
    }

}
