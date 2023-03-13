package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.app.PushApiInit;
import cn.cuptec.faros.entity.User;
import com.getui.push.v2.sdk.ApiHelper;
import com.getui.push.v2.sdk.GtApiConfiguration;
import com.getui.push.v2.sdk.api.PushApi;
import com.getui.push.v2.sdk.common.ApiResult;
import com.getui.push.v2.sdk.dto.req.Audience;
import com.getui.push.v2.sdk.dto.req.message.PushChannel;
import com.getui.push.v2.sdk.dto.req.message.PushDTO;
import com.getui.push.v2.sdk.dto.req.message.PushMessage;
import com.getui.push.v2.sdk.dto.req.message.android.AndroidDTO;
import com.getui.push.v2.sdk.dto.req.message.android.GTNotification;
import com.getui.push.v2.sdk.dto.req.message.android.ThirdNotification;
import com.getui.push.v2.sdk.dto.req.message.android.Ups;
import com.getui.push.v2.sdk.dto.req.message.ios.Alert;
import com.getui.push.v2.sdk.dto.req.message.ios.Aps;
import com.getui.push.v2.sdk.dto.req.message.ios.IosDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class UniAppPushService {
    @Resource
    private UserService userService;

    public void send(String title, String body, String userId, String url) {
        User user = userService.getById(userId);
        if (user == null || StringUtils.isEmpty(user.getCid())) {
            return;
        }
        PushApi pushApi = PushApiInit.getPushApi();
        //根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数
        pushDTO.setRequestId(System.currentTimeMillis() + "");
        /**** 设置个推通道参数 *****/
        PushMessage pushMessage = new PushMessage();
        pushDTO.setPushMessage(pushMessage);
        GTNotification notification = new GTNotification();
        pushMessage.setNotification(notification);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setClickType("startapp");
        notification.setUrl(url);
        /**** 设置个推通道参数，更多参数请查看文档或对象源码 *****/

        /**** 设置厂商相关参数 ****/
        PushChannel pushChannel = new PushChannel();
        pushDTO.setPushChannel(pushChannel);
        /*配置安卓厂商参数*/
        AndroidDTO androidDTO = new AndroidDTO();
        pushChannel.setAndroid(androidDTO);
        Ups ups = new Ups();
        androidDTO.setUps(ups);
        ThirdNotification thirdNotification = new ThirdNotification();
        ups.setNotification(thirdNotification);
        thirdNotification.setTitle(title);
        thirdNotification.setBody(body);
        thirdNotification.setClickType("startapp");
        thirdNotification.setUrl(url);
        // 两条消息的notify_id相同，新的消息会覆盖老的消息，取值范围：0-2147483647
        // thirdNotification.setNotifyId("11177");
        /*配置安卓厂商参数结束，更多参数请查看文档或对象源码*/

        /*设置ios厂商参数*/
        IosDTO iosDTO = new IosDTO();
        pushChannel.setIos(iosDTO);
        // 相同的collapseId会覆盖之前的消息
        //iosDTO.setApnsCollapseId("xxx");
        Aps aps = new Aps();
        iosDTO.setAps(aps);
        Alert alert = new Alert();
        aps.setAlert(alert);
        alert.setTitle(title);
        alert.setBody(body);
        /*设置ios厂商参数结束，更多参数请查看文档或对象源码*/

        /*设置接收人信息*/
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(user.getCid());
        /*设置接收人信息结束*/
        /**** 设置厂商相关参数，更多参数请查看文档或对象源码 ****/

        // 进行cid单推
        ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
        if (apiResult.isSuccess()) {
            // success
            System.out.println(apiResult.getData());
        } else {
            // failed
            System.out.println("code:" + apiResult.getCode() + ", msg: " + apiResult.getMsg());
        }


    }

}
