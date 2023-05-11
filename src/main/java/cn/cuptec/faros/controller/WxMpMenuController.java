package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.wx.WxMaProperties;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.config.wx.WxMpProperties;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.RoleMenuMapper;
import cn.cuptec.faros.mapper.UserMapper;
import cn.cuptec.faros.service.UserOrdertService;
import cn.cuptec.faros.service.UserService;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

//微信公众号菜单
@RestController
@AllArgsConstructor
@RequestMapping("/mp/menu")
@Slf4j
public class WxMpMenuController {
    private final StringRedisTemplate redisTemplate;
    private final WxMpProperties wxMpProperties;
    private final UserService userService;

    /**
     * 获取公众号菜单
     */
    @SneakyThrows
    @GetMapping
    @PreAuthorize("@pms.hasPermission('mp_wxmenu_get')")
    public RestResponse getById() {
        return RestResponse.ok(WxMpConfiguration.getWxMpService().getMenuService().menuGet());
    }

    @SneakyThrows
    @SysLog("新增微信菜单")
    @PostMapping("/save")
    //@PreAuthorize("@pms.hasPermission('mp_wxmenu_add')")
    public RestResponse save(@RequestBody WxMenu wxMenu) {
        return RestResponse.ok(WxMpConfiguration.getWxMpService().getMenuService().menuCreate(wxMenu));
    }

    @SneakyThrows
    @SysLog("删除微信菜单")
    @DeleteMapping("/del")
    //@PreAuthorize("@pms.hasPermission('mp_wxmenu_del')")
    public RestResponse del(@RequestParam("menuId") String menuId) {
        if (StringUtils.isNotEmpty(menuId)) {
            WxMpConfiguration.getWxMpService().getMenuService().menuDelete(menuId);
        } else {
            WxMpConfiguration.getWxMpService().getMenuService().menuDelete();
        }
        return RestResponse.ok();
    }

    @GetMapping("/preset")
    public RestResponse preset() throws WxErrorException {
        WxMenuButton wxMenuButton = new WxMenuButton();
        wxMenuButton.setName("个人中心");
        wxMenuButton.setType(WxConsts.MenuButtonType.VIEW);
        wxMenuButton.setUrl("http://pharos.ewj100.com/index.html#/my");
        WxMenu wxMenu = new WxMenu();
        wxMenu.setButtons(Arrays.asList(new WxMenuButton[]{wxMenuButton}));
        WxMpConfiguration.getWxMpService().getMenuService().menuCreate(wxMenu);
        return RestResponse.ok();
    }

    @GetMapping("/getAccessToken")
    public String getToken() {
        String access_token = redisTemplate.opsForValue().get("ACCESS_TOKEN");

//        if (!StringUtils.isEmpty(access_token)) {
//            return access_token;
//        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wxMpProperties.getAppId() + "&secret=" + wxMpProperties.getSecret();
        String result = HttpUtil.get(url);
        System.out.println(result + "============");
        WenXinInfo wenXinInfo = JSONObject.parseObject(result, WenXinInfo.class);
        //redisTemplate.opsForValue().set("ACCESS_TOKEN", wenXinInfo.getAccess_token(), wenXinInfo.getExpires_in(), TimeUnit.SECONDS);
        return wenXinInfo.getAccess_token();
    }

    /**
     * 创建用户标签
     */
    @PostMapping("/createtag")
    public RestResponse createTag(@RequestBody String body) {
        String token = getToken();

        String url = "https://api.weixin.qq.com/cgi-bin/tags/create?access_token=" + token;
        String params = "{ \"tag\" : { \"name\" : \"业务员\"}}";
        String post = HttpUtil.post(url, body);
        System.out.println(post + "==========");
        return RestResponse.ok(post);
    }
    /**
     * 查询创建的用户标签
     */
    @GetMapping("/getTag")
    public RestResponse getTag() {
        String token = getToken();

        String url = "https://api.weixin.qq.com/cgi-bin/tags/get?access_token=" + token;
        String result = HttpUtil.get(url);
        return RestResponse.ok(result);
    }

    /**
     * 为用户打标签
     */
    @GetMapping("/batchTagging")
    private RestResponse batchTagging(@RequestParam("tagId") int tagId, @RequestParam("mpOpenId") String mpOpenId) {


        UserTag userTag = new UserTag();
        userTag.setTagid(tagId);
        List<String> openIds = new ArrayList<>();
        openIds.add(mpOpenId);
        userTag.setOpenid_list(openIds);
        String result = batchTaggings(userTag);
        return RestResponse.ok(result);
    }

    public String batchTaggings(UserTag userTag) {
        String token = null;
        try {
            token = WxMpConfiguration.getWxMpService().getAccessToken();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        String url = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=" + token;
        String params = JSONObject.toJSONString(userTag);
        String post = HttpUtil.post(url, params);
        return post;
    }

    /**
     * 为用户取消标签
     */
    @GetMapping("/batchCancelTagging")
    public RestResponse batchCancelTagging(@RequestParam("tagId") int tagId, @RequestParam("mpOpenId") String mpOpenId) {
        String token = null;
        try {
            token = WxMpConfiguration.getWxMpService().getAccessToken();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        String url = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=" + token;
        UserTag userTag = new UserTag();
        userTag.setTagid(tagId);
        List<String> openIds = new ArrayList<>();
        openIds.add(mpOpenId);
        userTag.setOpenid_list(openIds);
        String params = JSONObject.toJSONString(userTag);
        String post = HttpUtil.post(url, params);
        System.out.println(post + "============");
        return RestResponse.ok(post);
    }

    /**
     * 获取用户身上的标签列表
     */
    @GetMapping("/gettagidlist")
    public RestResponse getTagIdList(@RequestParam(value = "openId", required = false) String openId) {
        String token = null;
        try {
            token = WxMpConfiguration.getWxMpService().getAccessToken();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        String url = "https://api.weixin.qq.com/cgi-bin/tags/getidlist?access_token=" + token;
        TagOpenId tagOpenId = new TagOpenId();
        tagOpenId.setOpenid(openId);
        String params = JSONObject.toJSONString(tagOpenId);
        String result = HttpUtil.post(url, params);
        System.out.println(result);
        return RestResponse.ok(result);
    }


    /**
     * 查询个性化菜单
     */
    @GetMapping("/getconditional")
    public RestResponse getConditional() {
        String token = getToken();
        String url = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + token;
        String result = HttpUtil.get(url);
        System.out.println(result);
        return RestResponse.ok(result);
    }

    /**
     * 根据openid获取用户自己能看到的菜单
     */
    @GetMapping("/getMenuByOpenId")
    public RestResponse getMenuByOpenId(@RequestParam(value = "openId", required = false) String openId) {
        String token = getToken();
        String url = "https://api.weixin.qq.com/cgi-bin/menu/trymatch?access_token=" + token;
        String params = " { \"user_id\" : \"oid410wbVLoaqNlCIC2OtvDY7-e0\"}";
        String result = HttpUtil.post(url, params);
        System.out.println(result);
        return RestResponse.ok();
    }

    /**
     * 批量修改业务员的标签
     */
    @GetMapping("/batchUpdateUserTag")
    public void batchUpdateUserTag() {
        Page<User> page = new Page<>();
        page.setSize(1000);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(18);
        IPage<User> userIPage = userService.queryUserByRole(roleIds, queryWrapper, page);
        List<User> records = userIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> openid_list = new ArrayList<>();
        for (User user : records) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                openid_list.add(user.getMpOpenId());
            }
        }
        UserTag userTag = new UserTag();
        userTag.setTagid(100);
        userTag.setOpenid_list(openid_list);
        batchTaggings(userTag);
        System.out.println(1);
    }
	public static void main(String[] args) {
        String url = "https://test.redadzukibeans.com/system/patientUser/scanLoginCode?macAdd=30:7b:c9:ad:15:cc" ;
        String result = HttpUtil.get(url);
        System.out.println(result);
	}


}
