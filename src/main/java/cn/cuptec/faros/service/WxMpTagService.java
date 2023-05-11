package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.config.wx.WxMpProperties;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserRole;
import cn.cuptec.faros.entity.UserTag;
import cn.cuptec.faros.entity.WenXinInfo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WxMpTagService {

    @Resource
    private UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final WxMpProperties wxMpProperties;


    @Transactional(rollbackFor = Exception.class)
    public void batchTaggings(List<UserRole> userRoles, Integer userId) {
        Integer tagId = 106;
        if (CollUtil.isNotEmpty(userRoles)) {
            List<Integer> salesmanRoleIds = CollUtil.toList(17, 18, 19, 7);
            List<Integer> doctorRoleIds = CollUtil.toList(20);
            List<Integer> userRoleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());

            if (CollUtil.intersection(userRoleIds, salesmanRoleIds).size() > 0) {
                tagId = 100;
                if (CollUtil.intersection(userRoleIds, doctorRoleIds).size() > 0) {
                    tagId = 105;
                }

            } else if (CollUtil.intersection(userRoleIds, doctorRoleIds).size() > 0) {
                tagId = 104;

            }
        }


        User user1 = userService.getById(userId);
        if (!StringUtils.isEmpty(user1.getMpOpenId())) {
            UserTag userTag = new UserTag();
            userTag.setTagid(tagId);
            List<String> openIds = new ArrayList<>();
            openIds.add(user1.getMpOpenId());
            userTag.setOpenid_list(openIds);

            String token = null;
            try {
                token = WxMpConfiguration.getWxMpService().getAccessToken();
            } catch (WxErrorException e) {
                e.printStackTrace();
            }
            String url = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=" + token;
            String params = JSONObject.toJSONString(userTag);
            String post = HttpUtil.post(url, params);

        }

    }


    public String getToken() {
        String access_token = redisTemplate.opsForValue().get("ACCESS_TOKEN");


        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wxMpProperties.getAppId() + "&secret=" + wxMpProperties.getSecret();
        String result = HttpUtil.get(url);
        System.out.println(result + "============");
        WenXinInfo wenXinInfo = JSONObject.parseObject(result, WenXinInfo.class);
        //redisTemplate.opsForValue().set("ACCESS_TOKEN", wenXinInfo.getAccess_token(), wenXinInfo.getExpires_in(), TimeUnit.SECONDS);
        return wenXinInfo.getAccess_token();
    }

    public static void main(String[] args) {
        List<Integer> salesmanRoleIds = CollUtil.toList(17, 18, 19, 7);
        List<Integer> doctorRoleIds = CollUtil.toList(7);
        System.out.println(CollUtil.intersection(doctorRoleIds, salesmanRoleIds).size() > 0);
    }

}