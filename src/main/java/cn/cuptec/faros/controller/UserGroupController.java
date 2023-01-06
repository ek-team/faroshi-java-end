package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.AddUserToGroupParam;
import cn.cuptec.faros.dto.PageResult;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mchange.v1.identicator.IdList;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者分组 管理
 */
@RestController
@RequestMapping("/userGroup")
public class UserGroupController extends AbstractBaseController<UserGroupService, UserGroup> {
    @Resource
    private UserFollowDoctorService userFollowDoctorService;//医生和患者的好友表
    @Resource
    private UserGroupRelationUserService userGroupRelationUserService;//分组和用户的关系表
    @Resource
    private UserService userService;

    /**
     * 添加患者分组
     *
     * @param userGroup
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody UserGroup userGroup) {
        userGroup.setCreateUserId(SecurityUtils.getUser().getId());
        service.save(userGroup);

        return RestResponse.ok(userGroup);
    }

    /**
     * 修改
     *
     * @param userGroup
     * @return
     */
    @PostMapping("/update")
    public RestResponse update(@RequestBody UserGroup userGroup) {
        service.updateById(userGroup);
        return RestResponse.ok(userGroup);
    }

    /**
     * 删除
     *
     * @return
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") Integer id) {
        List<UserGroupRelationUser> list = userGroupRelationUserService.list(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, id));
        List<UserFollowDoctor> userFollowDoctors = new ArrayList<>();
        for (UserGroupRelationUser userGroupRelationUser : list) {
            UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
            userFollowDoctor.setDoctorId(SecurityUtils.getUser().getId());
            userFollowDoctor.setUserId(userGroupRelationUser.getUserId());
            userFollowDoctors.add(userFollowDoctor);
        }
        userFollowDoctorService.saveBatch(userFollowDoctors);


        service.removeById(id);


        return RestResponse.ok();
    }

    /**
     * 添加患者到分组
     *
     * @return
     */
    @PostMapping("/addUserToGroup")
    public RestResponse addUserToGroup(@RequestBody AddUserToGroupParam param) {
        List<Integer> userIds = param.getUserIds();
        List<UserGroupRelationUser> userGroupRelationUsers = new ArrayList<>();
        for (Integer userId : userIds) {
            UserGroupRelationUser userGroupRelationUser = new UserGroupRelationUser();
            userGroupRelationUser.setUserId(userId);
            userGroupRelationUser.setUserGroupId(param.getUserGroupId());
            userGroupRelationUsers.add(userGroupRelationUser);
        }
        userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                .eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId())
                .in(UserFollowDoctor::getUserId, userIds));
        userGroupRelationUserService.saveBatch(userGroupRelationUsers);
        return RestResponse.ok();
    }

    /**
     * 移除患者该分组
     *
     * @return
     */

    @GetMapping("/removeUserToGroup")
    public RestResponse removeUserToGroup(@RequestParam("userId") Integer userId, @RequestParam("userGroupId") Integer userGroupId) {

        userGroupRelationUserService.remove(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, userGroupId)
                .eq(UserGroupRelationUser::getUserId, userId));
        //将该好友添加到未分组里
        UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
        userFollowDoctor.setDoctorId(SecurityUtils.getUser().getId());
        userFollowDoctor.setUserId(userId);
        userFollowDoctorService.save(userFollowDoctor);
        return RestResponse.ok();
    }

    /**
     * 查询患者分组 数量
     *
     * @return
     */
    @GetMapping("/getUserToGroupCount")
    public RestResponse getUserToGroupCount() {
        List<UserGroup> userGroups = service.list(new QueryWrapper<UserGroup>().lambda()
                .eq(UserGroup::getCreateUserId, SecurityUtils.getUser().getId()).orderByDesc(UserGroup::getSort));

        for (UserGroup userGroup : userGroups) {
            int count = userGroupRelationUserService.count(new QueryWrapper<UserGroupRelationUser>().lambda()
                    .eq(UserGroupRelationUser::getUserGroupId, userGroup.getId()));
            userGroup.setCount(count);
        }
        //查询未分组数量
        int count = userFollowDoctorService.count(new QueryWrapper<UserFollowDoctor>().lambda()
                .eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId()));

        UserGroup userGroup = new UserGroup();
        userGroup.setCount(count);
        userGroup.setName("未分组");
        userGroup.setId(-1);
        userGroups.add(0, userGroup);
        return RestResponse.ok(userGroups);
    }

    /**
     * 分页查询分组患者
     *
     * @return
     */
    @GetMapping("/pageQuery")
    public RestResponse getUserToGroupCount(@RequestParam("userGroupId") Integer userGroupId,
                                            @RequestParam("pageNum") Integer pageNum,
                                            @RequestParam("pageSize") Integer pageSize) {
        IPage page = new Page(pageNum, pageSize);
        if (userGroupId == -1) {
            //查询未分组
            LambdaQueryWrapper<UserFollowDoctor> wrapper = Wrappers.<UserFollowDoctor>lambdaQuery()
                    .eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId());
            IPage page1 = userFollowDoctorService.page(page, wrapper);
            List<UserFollowDoctor> records = page1.getRecords();
            if (!CollectionUtils.isEmpty(records)) {
                List<Integer> userIds = records.stream().map(UserFollowDoctor::getUserId)
                        .collect(Collectors.toList());
                List<User> users = (List<User>) userService.listByIds(userIds);

                page1.setRecords(users);
            }
            return RestResponse.ok(page1);
        } else {
            //查询分组
            LambdaQueryWrapper<UserGroupRelationUser> wrapper = Wrappers.<UserGroupRelationUser>lambdaQuery()
                    .eq(UserGroupRelationUser::getUserGroupId, userGroupId);
            IPage page1 = userGroupRelationUserService.page(page, wrapper);
            List<UserGroupRelationUser> records = page1.getRecords();

            if (!CollectionUtils.isEmpty(records)) {
                List<Integer> userIds = records.stream().map(UserGroupRelationUser::getUserId)
                        .collect(Collectors.toList());
                List<User> users = (List<User>) userService.listByIds(userIds);

                page1.setRecords(users);
            }

            return RestResponse.ok(page1);
        }


    }

    /**
     * 医生端查询患者列表 按字母排序
     *
     * @return
     */
    @GetMapping("/pageQueryPatientUserSort")
    public RestResponse getUserToGroupCount(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize) {
        List<UserFollowDoctor> userFollowDoctors = userFollowDoctorService.pageQueryPatientUserSort(pageNum, pageSize, SecurityUtils.getUser().getId());
        int count = userFollowDoctorService.pageQueryPatientUserSortTotal(SecurityUtils.getUser().getId());
        //按字母分组
        TreeMap<String, List<UserFollowDoctor>> letterMap = new TreeMap<>((s1, s2) -> {
            //#号组放到最后
            if ("#".equals(s1) ) {
                return 1;
            }
            if ("#".equals(s2) ) {
                return -1;
            }
            return s1.compareTo(s2);
        });
        if(!CollectionUtils.isEmpty(userFollowDoctors)){

            for (UserFollowDoctor t : userFollowDoctors) {
                String pinYinFirstLetter = getPinYinFirstCharFirstLetter(t.getNickname());
                if (!letterMap.containsKey(pinYinFirstLetter)) {
                    if (pinYinFirstLetter.matches("[A-Z]")) {
                        letterMap.put(pinYinFirstLetter, new ArrayList<UserFollowDoctor>());
                    } else {
                        letterMap.put("#", new ArrayList<UserFollowDoctor>());
                    }
                }
            }

            for (Map.Entry<String, List<UserFollowDoctor>> next : letterMap.entrySet()) {
                List<UserFollowDoctor> listTemp = new ArrayList<UserFollowDoctor>();
                for (UserFollowDoctor t : userFollowDoctors) {
                    String pinYinFirstLetter = getPinYinFirstCharFirstLetter(t.getNickname());
                    if (next.getKey().equals("#")) {
                        if (StringUtils.isNotBlank(pinYinFirstLetter) && !pinYinFirstLetter.matches("[A-Z]|[a-z]")) {
                            listTemp.add(t);
                            continue;
                        }
                    }
                    if (StringUtils.isNotBlank(pinYinFirstLetter) && next.getKey().equalsIgnoreCase(pinYinFirstLetter)) {
                        listTemp.add(t);
                    }
                }
                List<UserFollowDoctor> value = next.getValue();
                value.addAll(listTemp);
            }

        }
        PageResult result=new PageResult();
        result.setTotal(count);
        result.setRecords(letterMap);
        return RestResponse.ok(result);
    }
    /**
     * @description: 将字符串转成拼音,只要首字符的首字母
     * @params:
     * @return:
     */
    public static String getPinYinFirstCharFirstLetter(String chinese) {
        if (StringUtils.isBlank(chinese)){
            return "";
        }
        char spell = chinese.toCharArray()[0];
        String pinYin = String.valueOf(spell);
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        //无语调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        if (spell > 128) {
            try {
                String[] strings = PinyinHelper.toHanyuPinyinStringArray(spell, defaultFormat);
                if (ArrayUtils.isNotEmpty(strings)) {
                    pinYin = String.valueOf(strings[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                return pinYin;
            }
        }
        return pinYin.toUpperCase();
    }
    @Override
    protected Class<UserGroup> getEntityClass() {
        return UserGroup.class;
    }
}
