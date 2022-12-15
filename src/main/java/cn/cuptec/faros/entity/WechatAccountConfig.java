package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/16 15:25
 */
@TableName("`wechat_account_config`")
@Data
public class WechatAccountConfig {

    @TableId(type = IdType.AUTO)
    private int id;
    /**
     * 公众账号appid
     * 获取地址 https://mp.weixin.qq.com
     */
    private String mpAppId;
    /**
     * 公众号的 appsecret
     */
    private String mpSecret;
    /**
     * 用户id
     */
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private int uid;

    /**
     * 小程序appId
     * 获取地址 https://mp.weixin.qq.com
     */
    private String miniAppId;

    /**
     * 小程序appSecret
     */
    private String miniAppSecret;

    /**
     * 商户号
     * 获取地址 https://pay.weixin.qq.com
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;
    /**
     * 商户证书路径
     */
    private String keyPath;
    /**
     * app应用appid
     * 获取地址 https://open.weixin.qq.com
     */
    private String appAppId;
}
