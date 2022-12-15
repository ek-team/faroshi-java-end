package cn.cuptec.faros.vo;

import lombok.Data;

/**
 * 社交登录结果
 */
@Data
public class SocialLoginResult {

    public SocialLoginResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    /**
     * 是否登录成功
     */
    private boolean success;

    /**
     * 登录结果数据
     * 登录成功则返回token
     * 登录失败，返回其他信息
     */
    private Object data;

}
