package cn.cuptec.faros.config.security.exception;

import lombok.Getter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * 用户未注册
 * 需要注册后登录
 * 主要用于小程序等得预登录
 */
public class WxNotRegException extends OAuth2Exception {

    @Getter
    private Object preRegData;

    public WxNotRegException(String msg, Object preRegData) {
        super(msg);
        this.preRegData = preRegData;
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "not_reged_exception";
    }

    @Override
    public int getHttpErrorCode() {
        return 100401;
    }

}
