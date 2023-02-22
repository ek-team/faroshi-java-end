package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.AddUserToGroupParam;
import cn.cuptec.faros.dto.PageResult;
import cn.cuptec.faros.dto.SearchUserGroupDto;
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
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.print.DocFlavor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者分组 管理
 */
@Slf4j
@RestController
@RequestMapping("/userGroup")
public class UserGroupController extends AbstractBaseController<UserGroupService, UserGroup> {
    @Resource
    private UserFollowDoctorService userFollowDoctorService;//医生和患者的好友表
    @Resource
    private UserGroupRelationUserService userGroupRelationUserService;//分组和用户的关系表
    @Resource
    private UserService userService;
    @Resource
    private UserDoctorRelationService userDoctorRelationService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;

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
        if (!CollectionUtils.isEmpty(userGroup.getUserIds())) {
            List<Integer> userIds = userGroup.getUserIds();
            List<UserGroupRelationUser> userGroupRelationUsers = new ArrayList<>();
            for (Integer userId : userIds) {
                UserGroupRelationUser userGroupRelationUser = new UserGroupRelationUser();
                userGroupRelationUser.setUserId(userId);
                userGroupRelationUser.setUserGroupId(userGroup.getId());
                userGroupRelationUsers.add(userGroupRelationUser);
            }
            if (userGroup.getTeamId() == null) {
                //团队分组
                userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getDoctorId, userGroup.getCreateUserId())
                        .in(UserFollowDoctor::getUserId, userIds));
            } else {
                //个人分组
                userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getTeamId, userGroup.getTeamId())
                        .in(UserFollowDoctor::getUserId, userIds));
            }

            userGroupRelationUserService.saveBatch(userGroupRelationUsers);


        }
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
        log.info("修改患者分组");
        service.updateById(userGroup);
        UserGroup byId = service.getById(userGroup.getId());
        //先将原先的这个分组的患者 移到 未分组里
        List<UserGroupRelationUser> list = userGroupRelationUserService.list(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, userGroup.getId()));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> userIds = list.stream().map(UserGroupRelationUser::getUserId)
                    .collect(Collectors.toList());
            List<UserFollowDoctor> userFollowDoctors = new ArrayList<>();
            if (byId.getTeamId() != null) {
                for (Integer userId : userIds) {
                    UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                    userFollowDoctor.setTeamId(byId.getTeamId());
                    userFollowDoctor.setUserId(userId);
                    userFollowDoctors.add(userFollowDoctor);
                }

            } else {
                for (Integer userId : userIds) {
                    UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                    userFollowDoctor.setDoctorId(byId.getCreateUserId());
                    userFollowDoctor.setUserId(userId);
                    userFollowDoctors.add(userFollowDoctor);
                }
            }
            userFollowDoctorService.saveBatch(userFollowDoctors);
        }


        userGroupRelationUserService.remove(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, userGroup.getId()));
        if (!CollectionUtils.isEmpty(userGroup.getUserIds())) {
            List<Integer> userIds = userGroup.getUserIds();
            List<UserGroupRelationUser> userGroupRelationUsers = new ArrayList<>();
            for (Integer userId : userIds) {
                UserGroupRelationUser userGroupRelationUser = new UserGroupRelationUser();
                userGroupRelationUser.setUserId(userId);
                userGroupRelationUser.setUserGroupId(userGroup.getId());
                userGroupRelationUsers.add(userGroupRelationUser);
            }
            if (byId.getTeamId() == null) {
                //团队分组
                userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getDoctorId, byId.getCreateUserId())
                        .in(UserFollowDoctor::getUserId, userIds));
            } else {
                //个人分组
                userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getTeamId, byId.getTeamId())
                        .in(UserFollowDoctor::getUserId, userIds));
            }

            userGroupRelationUserService.saveBatch(userGroupRelationUsers);


        }
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

        UserGroup userGroup = service.getById(id);
        List<UserFollowDoctor> userFollowDoctors = new ArrayList<>();

        if (userGroup.getTeamId() == null) {
            //个人分组
            for (UserGroupRelationUser userGroupRelationUser : list) {
                UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                userFollowDoctor.setDoctorId(userGroup.getCreateUserId());
                userFollowDoctor.setUserId(userGroupRelationUser.getUserId());
                userFollowDoctors.add(userFollowDoctor);
            }
        } else {
            //团队分组
            for (UserGroupRelationUser userGroupRelationUser : list) {
                UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                userFollowDoctor.setTeamId(userGroup.getTeamId());
                userFollowDoctor.setUserId(userGroupRelationUser.getUserId());
                userFollowDoctors.add(userFollowDoctor);
            }
        }
        userFollowDoctorService.saveBatch(userFollowDoctors);
        service.removeById(id);
        userGroupRelationUserService.remove(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, id));

        return RestResponse.ok();
    }

    /**
     * 获取分组详情
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {
        UserGroup byId = service.getById(id);
        List<UserGroupRelationUser> list = userGroupRelationUserService.list(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, id));
        if (!CollectionUtils.isEmpty(list)) {

            List<Integer> userIds = list.stream().map(UserGroupRelationUser::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            byId.setUsers(users);
        }
        return RestResponse.ok(byId);
    }

    /**
     * 添加患者到分组
     *
     * @return
     */
    @PostMapping("/addUserToGroup")
    public RestResponse addUserToGroup(@RequestBody AddUserToGroupParam param) {
        log.info("添加患者到分组");
        List<Integer> userIds = param.getUserIds();
        List<UserGroupRelationUser> userGroupRelationUsers = new ArrayList<>();
        for (Integer userId : userIds) {
            UserGroupRelationUser userGroupRelationUser = new UserGroupRelationUser();
            userGroupRelationUser.setUserId(userId);
            userGroupRelationUser.setUserGroupId(param.getUserGroupId());
            userGroupRelationUsers.add(userGroupRelationUser);
        }
        UserGroup userGroup = service.getById(param.getUserGroupId());
        if (userGroup.getTeamId() == null) {
            //团队分组
            userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                    .eq(UserFollowDoctor::getTeamId, userGroup.getTeamId())
                    .in(UserFollowDoctor::getUserId, userIds));
        } else {
            //个人分组
            userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                    .eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId())
                    .in(UserFollowDoctor::getUserId, userIds));
        }

        userGroupRelationUserService.saveBatch(userGroupRelationUsers);
        return RestResponse.ok();
    }

    /**
     * 移动患者到分组
     */
    @GetMapping("/moveUserToGroup")
    public RestResponse moveUserToGroup(@RequestParam("userId") Integer userId, @RequestParam("targetGroupId") Integer targetGroupId
    ) {
        log.info("移动患者到分组");
        List<UserGroup> userGroupList = service.list(new QueryWrapper<UserGroup>().lambda()
                .eq(UserGroup::getCreateUserId, SecurityUtils.getUser().getId()));  //医生所创建的分组
        if (!CollectionUtils.isEmpty(userGroupList)) {
            List<Integer> userGroupIds = userGroupList.stream().map(UserGroup::getId)
                    .collect(Collectors.toList());
            userGroupRelationUserService.remove(new QueryWrapper<UserGroupRelationUser>().lambda()
                    .eq(UserGroupRelationUser::getUserId, userId)
                    .in(UserGroupRelationUser::getUserGroupId, userGroupIds));
        }
        if (targetGroupId.equals(-1)) {
            //移到未分组

            UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
            userFollowDoctor.setUserId(userId);
            userFollowDoctor.setDoctorId(SecurityUtils.getUser().getId());
            userFollowDoctorService.save(userFollowDoctor);
        } else {
            //移动到别的分组
            //添加到新的分组
            UserGroupRelationUser userGroupRelationUser = new UserGroupRelationUser();
            userGroupRelationUser.setUserId(userId);
            userGroupRelationUser.setUserGroupId(targetGroupId);
            userGroupRelationUserService.save(userGroupRelationUser);
        }

        return RestResponse.ok();
    }

    /**
     * 移除患者该分组
     *
     * @return
     */

    @GetMapping("/removeUserToGroup")
    public RestResponse removeUserToGroup(@RequestParam("userId") Integer userId, @RequestParam("userGroupId") Integer userGroupId) {
        log.info("移除患者该分组");
        userGroupRelationUserService.remove(new QueryWrapper<UserGroupRelationUser>().lambda()
                .eq(UserGroupRelationUser::getUserGroupId, userGroupId)
                .eq(UserGroupRelationUser::getUserId, userId));
        //将该好友添加到未分组里
        UserGroup userGroup = service.getById(userGroupId);
        UserFollowDoctor userFollowDoctor = new UserFollowDoctor();

        if (userGroup.getTeamId() == null) {
            //团队分组
            userFollowDoctor.setTeamId(userGroup.getTeamId());
            userFollowDoctor.setUserId(userId);
        } else {
            //个人分组
            userFollowDoctor.setDoctorId(SecurityUtils.getUser().getId());
            userFollowDoctor.setUserId(userId);
        }
        userFollowDoctorService.save(userFollowDoctor);
        return RestResponse.ok();
    }

    @GetMapping("/searchUserGroup")
    public RestResponse searchUserGroup(@RequestParam("key") String key) {
        List<UserGroup> list = service.list(new QueryWrapper<UserGroup>().lambda()
                .like(UserGroup::getName, key)
                .like(UserGroup::getCreateUserId, SecurityUtils.getUser().getId()));

        SearchUserGroupDto searchUserGroupDto = new SearchUserGroupDto();
        searchUserGroupDto.setUserGroupList(new ArrayList<>());
        searchUserGroupDto.setUserList(new ArrayList<>());
        if (!CollectionUtils.isEmpty(list)) {
            //查询分组人数
            for (UserGroup userGroup : list) {
                int count = userGroupRelationUserService.count(new QueryWrapper<UserGroupRelationUser>().lambda()
                        .eq(UserGroupRelationUser::getUserGroupId, userGroup.getId()));
                userGroup.setCount(count);
            }
            searchUserGroupDto.setUserGroupList(list);

        }
        //搜索患者
        List<User> userList = userService.list(new QueryWrapper<User>().lambda()
                .like(User::getPatientName, key));
        if (!CollectionUtils.isEmpty(userList)) {
            List<Integer> userIds = userList.stream().map(User::getId)
                    .collect(Collectors.toList());

            List<UserDoctorRelation> userDoctorRelationList = userDoctorRelationService.list(new QueryWrapper<UserDoctorRelation>().lambda()
                    .in(UserDoctorRelation::getUserId, userIds)
                    .eq(UserDoctorRelation::getDoctorId, SecurityUtils.getUser().getId()));
            if (!CollectionUtils.isEmpty(userDoctorRelationList)) {
                List<User> users = new ArrayList<>();
                Map<Integer, User> userMap = userList.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
                for (User user : userList) {
                    String idCard = user.getIdCard();
                    if (!StringUtils.isEmpty(idCard)) {
                        Map<String, String> map = getAge(idCard);
                        user.setAge(map.get("age"));

                        user.setBirthday(map.get("birthday"));
                        user.setSexCode(map.get("sexCode"));//1-男0-女

                    }
                }
                for (UserDoctorRelation userDoctorRelation : userDoctorRelationList) {
                    users.add(userMap.get(userDoctorRelation.getUserId()));
                }
                searchUserGroupDto.setUserList(users);
            }
        }

        return RestResponse.ok(searchUserGroupDto);
    }

    private static Map<String, String> getAge(String idCard) {
        String birthday = "";
        String age = "";
        Integer sexCode = 0;

        int year = Calendar.getInstance().get(Calendar.YEAR);
        char[] number = idCard.toCharArray();
        boolean flag = true;

        if (number.length == 15) {
            for (int x = 0; x < number.length; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        } else if (number.length == 18) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        }

        if (flag && idCard.length() == 15) {
            birthday = "19" + idCard.substring(6, 8) + "-"
                    + idCard.substring(8, 10) + "-"
                    + idCard.substring(10, 12);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 3, idCard.length())) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt("19" + idCard.substring(6, 8))) + "";
        } else if (flag && idCard.length() == 18) {
            birthday = idCard.substring(6, 10) + "-"
                    + idCard.substring(10, 12) + "-"
                    + idCard.substring(12, 14);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 4, idCard.length() - 1)) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt(idCard.substring(6, 10))) + "";
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("birthday", birthday);
        map.put("age", age);
        map.put("sexCode", sexCode + "");
        return map;
    }

    /**
     * 查询患者分组 数量
     *
     * @return
     */
    @GetMapping("/getUserToGroupCount")
    public RestResponse getUserToGroupCount(@RequestParam(value = "teamId", required = false) Integer teamId) {
        log.info("查询患者分组 数量" + teamId);
        LambdaQueryWrapper<UserGroup> userGroupLambdaQueryWrapper = new QueryWrapper<UserGroup>().lambda();
        if (teamId != null) {
            userGroupLambdaQueryWrapper.eq(UserGroup::getTeamId, teamId);
        } else {
            userGroupLambdaQueryWrapper.isNull(UserGroup::getTeamId);
            userGroupLambdaQueryWrapper.eq(UserGroup::getCreateUserId,SecurityUtils.getUser().getId());
        }
        userGroupLambdaQueryWrapper.orderByDesc(UserGroup::getSort);
        List<UserGroup> userGroups = service.list(userGroupLambdaQueryWrapper);

        for (UserGroup userGroup : userGroups) {
            int count = userGroupRelationUserService.count(new QueryWrapper<UserGroupRelationUser>().lambda()
                    .eq(UserGroupRelationUser::getUserGroupId, userGroup.getId()));
            userGroup.setCount(count);
        }
        //查询未分组数量
        LambdaQueryWrapper<UserFollowDoctor> eq = new QueryWrapper<UserFollowDoctor>().lambda();
        if (teamId != null) {
            eq.eq(UserFollowDoctor::getTeamId, teamId);
        } else {
            eq.eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId());
        }

        int count = userFollowDoctorService.count(eq);

        UserGroup userGroup = new UserGroup();
        userGroup.setCount(count);
        userGroup.setName("未分组");
        userGroup.setId(-1);
        userGroups.add(0, userGroup);
        return RestResponse.ok(userGroups);
    }

    /**
     * 查询可分配分组的患者
     *
     * @return
     */
    @GetMapping("/getUserNoGroup")
    public RestResponse getUserNoGroup(@RequestParam(value = "teamId", required = false) Integer teamId) {

        //查询未分组数量
        LambdaQueryWrapper<UserFollowDoctor> eq = new QueryWrapper<UserFollowDoctor>().lambda();
        if (teamId != null) {
            eq.eq(UserFollowDoctor::getTeamId, teamId);
        } else {
            eq.eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId());
        }

        List<UserFollowDoctor> userFollowDoctorList = userFollowDoctorService.list(eq);
        if (CollectionUtils.isEmpty(userFollowDoctorList)) {
            return RestResponse.ok();
        }
        List<Integer> userIds = userFollowDoctorList.stream().map(UserFollowDoctor::getUserId)
                .collect(Collectors.toList());

        return RestResponse.ok(userService.listByIds(userIds));
    }

    /**
     * 分页查询分组患者
     * userGroupId=-1查询未分组的患者
     *
     * @return
     */
    @GetMapping("/pageQuery")
    public RestResponse getUserToGroupCount(@RequestParam("userGroupId") Integer userGroupId,
                                            @RequestParam("pageNum") Integer pageNum,
                                            @RequestParam("pageSize") Integer pageSize,
                                            @RequestParam(value = "teamId", required = false) Integer teamId) {
        IPage page = new Page(pageNum, pageSize);
        if (userGroupId == -1) {
            //查询未分组
            LambdaQueryWrapper<UserFollowDoctor> wrapper = Wrappers.<UserFollowDoctor>lambdaQuery();
            if (teamId != null) {
                wrapper.eq(UserFollowDoctor::getTeamId, teamId);
            } else {
                wrapper.eq(UserFollowDoctor::getDoctorId, SecurityUtils.getUser().getId());
            }
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
            if ("#".equals(s1)) {
                return 1;
            }
            if ("#".equals(s2)) {
                return -1;
            }
            return s1.compareTo(s2);
        });
        if (!CollectionUtils.isEmpty(userFollowDoctors)) {

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
        PageResult result = new PageResult();
        result.setTotal(count);
        result.setRecords(letterMap);
        return RestResponse.ok(result);
    }

    /**
     * @description: 将字符串转成拼音, 只要首字符的首字母
     * @params:
     * @return:
     */
    public static String getPinYinFirstCharFirstLetter(String chinese) {
        if (StringUtils.isBlank(chinese)) {
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
