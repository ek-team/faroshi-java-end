package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.dto.UserPwdDTO;
import cn.cuptec.faros.entity.Menu;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import cn.cuptec.faros.entity.Role;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.WxMpMenuController;
import cn.cuptec.faros.dto.ChangeDoctorDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.UserMapper;
import cn.cuptec.faros.service.handler.login.LoginHandler;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.jws.soap.SOAPBinding;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Resource
    private MenuService tenantMenuService;
    @Resource
    private RoleService roleService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private HospitalDoctorRelationService hospitalDoctorRelationService;
    @Resource
    private WxMpMenuController wxMpMenuController;
    @Resource
    private Map<String, LoginHandler> loginHandlerMap;
    @Resource
    private SalesmanPayChannelService salesmanPayChannelService;
    @Resource
    private WechatAccountConfigService wechatAccountConfigService;
    @Resource
    private SalesmanRetrieveAddressService salesmanRetrieveAddressService;
    @Resource
    private ProtocolService protocolService;
    @Resource
    private SupportProductListService supportProductListService;
    @Resource
    private CustomProductService customProductService;
    @Resource
    private TokenService tokenService;

    @Resource
    private UserDoctorRelationService userDoctorRelationService;

    @Resource
    private WxMpTagService wxMpTagService;
    @Resource
    private PatientUserService patientUserService;
    /**
     *
     */
    public User getUserINfo(Integer uid) {

        User user = baseMapper.selectById(uid);
        if(!StringUtils.isEmpty(user.getPatientId())){
            PatientUser patientUser = patientUserService.getById(user.getPatientId());
            if(patientUser!=null){
                user.setIdCard(patientUser.getIdCard());
                user.setPatientName(patientUser.getName());
            }

        }
        String idCard = user.getIdCard();
        if (!org.apache.commons.lang3.StringUtils.isEmpty(idCard)) {
            Map<String, String> map = getAge(idCard);
            user.setAge(map.get("age"));

            user.setBirthday(map.get("birthday"));
            user.setSexCode(map.get("sexCode"));//1-男0-女

        }

        return user;
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


    //租协议配置
    private Boolean checkProtocol(List<Integer> uids, int deptId) {
        //查询支持的产品列表
        List<SupportProductList> supportProductLists = supportProductListService.list(Wrappers.<SupportProductList>lambdaQuery().eq(SupportProductList::getDepId, deptId));
        if (CollectionUtils.isEmpty(supportProductLists)) {
            return true;
        }

//        List<Protocol> protocols = protocolService.list(Wrappers.<Protocol>lambdaQuery().in(Protocol::getUid, uids));
//        if (CollectionUtils.isEmpty(protocols)) {
//            return false;
//        }
//        if (supportProductLists.size() > protocols.size()) {
//            return false;
//        }
        return true;
    }

    //查询是否有回收规则设置
    private Boolean checkCustomProduct(int depId) {
        //查询支持的产品列表
        List<SupportProductList> supportProductLists = supportProductListService.list(Wrappers.<SupportProductList>lambdaQuery().eq(SupportProductList::getDepId, depId));
        if (CollectionUtils.isEmpty(supportProductLists)) {
            return true;
        }

        List<Integer> productIds = supportProductLists.stream().map(SupportProductList::getProductId)
                .collect(Collectors.toList());

        List<CustomProduct> customProducts = customProductService.list(Wrappers.<CustomProduct>lambdaQuery().eq(CustomProduct::getDeptId, depId));
        if (CollectionUtils.isEmpty(customProducts)) {
            return false;
        }
        if (productIds.size() > customProducts.size()) {
            return false;
        }
        return true;
    }


    //查询是否 有个人收款码或者 商户服务号收款
    private Boolean checkPayChannel(List<Integer> uids) {
        SalesmanPayChannel salesmanPayChannel = salesmanPayChannelService.getOne(Wrappers.<SalesmanPayChannel>lambdaQuery()
                .in(SalesmanPayChannel::getSalesmanId, uids).last(" limit 1"));
        if (salesmanPayChannel == null) {
            return false;
        }


        if (salesmanPayChannel.getPayType() == 1 || salesmanPayChannel.getPayType() == 0) {
            //个人收款码
            if (StringUtils.isEmpty(salesmanPayChannel.getRevMoneyPicUrl())) {
                return false;
            }
        } else {
            //商户收款
            WechatAccountConfig wechatAccountConfig = wechatAccountConfigService.getOne(Wrappers.<WechatAccountConfig>lambdaQuery().in(WechatAccountConfig::getUid, uids).last(" limit 1"));
            if (wechatAccountConfig == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 查询是医生的用户
     */
    public IPage<User> queryUserByRole(List<Integer> roleIds, QueryWrapper<User> queryWrapper, IPage<User> page) {
        List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                .in(UserRole::getRoleId, roleIds));
        if (CollectionUtils.isEmpty(userRoles)) {
            return null;
        }
        List<Integer> userIds = userRoles.stream().map(UserRole::getUserId)
                .collect(Collectors.toList());
        queryWrapper.in("user.id", userIds);
        IPage<User> scopedUsers = baseMapper.pageScopedUser(page, queryWrapper, new DataScope());
        List<User> records = scopedUsers.getRecords();
        List<Integer> uids = records.stream().map(user -> user.getId()).collect(Collectors.toList());

        if (uids.size() > 0) {
            List<UserRole> userRoles1 = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                    .in(UserRole::getUserId, uids)
            );
            List<Integer> roleIds1 = userRoles1.stream().map(userRole -> userRole.getRoleId()).collect(Collectors.toList());
            if (roleIds1.size() > 0) {
                List<Role> roles = (List<Role>) roleService.listByIds(roleIds1);
                records.forEach(user -> {
                    List<Integer> userRoleIds = new ArrayList<>();
                    userRoles1.forEach(userRole -> {
                        if (userRole.getUserId().intValue() == user.getId()) {
                            userRoleIds.add(userRole.getRoleId());
                        }
                    });
                    List<Role> urs = new ArrayList<>();
                    roles.forEach(role -> {
                        if (userRoleIds.contains(role.getId().intValue())) {
                            urs.add(role);
                        }
                    });
                    user.setRoles(urs.toArray(new Role[0]));
                });
            } else {
                records.forEach(user -> {
                    user.setRoles(new ArrayList<Role>().toArray(new Role[0]));
                });
            }
        }
        return scopedUsers;
    }

    /**
     * 通过ID查询用户信息
     */
    public User selectUserVoById(Integer id) {
        User user = baseMapper.selectById(id);

        return refactByUser(user);
    }

    //根据用户信息补充用户权限角色信息
    public User refactByUser(User user) {
        if (user != null) {
            List<Role> roles = roleService.findRolesByUserId(user.getId());
            user.setRoles(roles.toArray(new Role[roles.size()]));
            if (roles.size() > 0) {
                List<Integer> roleIds = roles.stream().map(role -> role.getId()).collect(Collectors.toList());
                List<Menu> menus = roleService.listMenusByRoleIds(roleIds);
                user.setPermissions(menus.toArray(new Menu[menus.size()]));
            }
        }

        return user;
    }

//    public IPage<User> queryUserByDeptAndNoRole(Page<User> page, Wrapper<User> wrapper) {
//        return baseMapper.queryUserByDeptAndNoRole(page, wrapper);
//    }

    public IPage<User> pageScopedUserVo(IPage<User> page, Wrapper<User> wrapper) {

        IPage<User> scopedUsers = baseMapper.queryUserByDeptAndNoRole(page, wrapper, new DataScope());
        List<User> records = scopedUsers.getRecords();
        List<Integer> uids = records.stream().map(user -> user.getId()).collect(Collectors.toList());
        if (uids.size() > 0) {
            List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                    .in(UserRole::getUserId, uids)
            );
            List<Integer> roleIds = userRoles.stream().map(userRole -> userRole.getRoleId()).collect(Collectors.toList());
            if (roleIds.size() > 0) {
                List<Role> roles = (List<Role>) roleService.listByIds(roleIds);
                records.forEach(user -> {
                    List<Integer> userRoleIds = new ArrayList<>();
                    userRoles.forEach(userRole -> {
                        if (userRole.getUserId().equals(user.getId())) {
                            userRoleIds.add(userRole.getRoleId());
                        }
                    });
                    List<Role> urs = new ArrayList<>();
                    roles.forEach(role -> {
                        if (userRoleIds.contains(role.getId().intValue())) {
                            urs.add(role);
                        }
                    });
                    user.setRoles(urs.toArray(new Role[0]));
                });
            } else {
                records.forEach(user -> {
                    user.setRoles(new ArrayList<Role>().toArray(new Role[0]));
                });
            }
        }
        userDoctorRelationService.getDoctorByUserList(records);
        return scopedUsers;
    }

    /**
     * 保存用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(User user) {

        User temuser = baseMapper.getPhoneIsExist(user.getPhone());


        if (temuser != null && "0".equals(temuser.getDelFlag())) {
            throw new InnerException("手机号已被使用");
        }
        user.setDelFlag(CommonConstants.STATUS_NORMAL);
        if (user.getPassword() != null)
            user.setPassword(ENCODER.encode(user.getPassword()));

        if (temuser == null) super.save(user);
        else {
            userRoleService.deleteByUserId(temuser.getId());
            user.setId(temuser.getId());
            baseMapper.updateUserById(user);
        }
        Role[] roles = user.getRoles();
        if (roles != null) {

            List<UserRole> userRoleList = Arrays.stream(roles)
                    .map(role -> {
                        UserRole userRole = new UserRole();
                        userRole.setUserId(user.getId());
                        userRole.setRoleId(role.getId());
                        return userRole;
                    })
                    .collect(Collectors.toList());
            userRoleService.saveBatch(userRoleList);
            wxMpTagService.batchTaggings(userRoleList, user.getId());
        }


        return Boolean.TRUE;
    }

    /**
     * 删除用户
     *
     * @param user 用户
     * @return Boolean
     */
    @CacheEvict(value = CacheConstants.USER_DETAILS, key = "#user.id")
    @Transactional
    public Boolean deleteUserById(User user) {
        //userRoleService.deleteByUserId(user.getId());
        this.removeById(user.getId());
        tokenService.delTokenByUserId(user.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(value = CacheConstants.USER_DETAILS, key = "#user.id")
    @Transactional
    public Boolean updateUser(User user) {
        if (StrUtil.isNotBlank(user.getPhone())) {
            User temuser = baseMapper.getPhoneIsExist(user.getPhone());
            if (temuser != null && "0".equals(temuser.getDelFlag()) && !temuser.getId().equals(user.getId())) {
                throw new InnerException("手机号已被使用");
            } else if (temuser != null && "1".equals(temuser.getDelFlag()) && !temuser.getId().equals(user.getId())) {
                int i = this.baseMapper.updateUserIsDelSetPhoneNull(temuser.getId());
                if (i == 0) throw new InnerException("手机号已被使用");
            }

        }

        user.setUpdateTime(LocalDateTime.now());
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(ENCODER.encode(user.getPassword()));
        }
        this.updateById(user);
        if (user.getRoles().length > 0) {

            List<UserRole> userRoles = new ArrayList<>();
            for (Role role : user.getRoles()) {
                if (role.getId() != null) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(role.getId());
                    userRoles.add(userRole);
                }

            }
            if (!CollectionUtils.isEmpty(userRoles)) {
                userRoleService.remove(Wrappers.<UserRole>update().lambda()
                        .eq(UserRole::getUserId, user.getId()));

                userRoleService.saveBatch(userRoles);
                wxMpTagService.batchTaggings(userRoles, user.getId());
            }


        }


        return Boolean.TRUE;
    }

    /**
     * 根据入参查询用户信息
     *
     * @param inStr TYPE@code
     * @return
     */
    public User getBySocialParam(String inStr) {
        String[] inStrs = inStr.split(StringPool.AT);
        //获取登录类型
        String type = inStrs[0];
        //登录凭证字符串
        String loginStr = inStr.replace(inStrs[0] + StringPool.AT, "");
        return loginHandlerMap.get(type).handle(loginStr);
    }

    /**
     * 根据角色查询
     *
     * @param page
     * @param queryWrapper
     * @param roleId
     * @return
     */
    public IPage<User> pageByRoleId(Page<User> page, QueryWrapper queryWrapper, int roleId) {
        List<User> users = baseMapper.pageByRoleId(page, queryWrapper, roleId, new DataScope());
        if (page == null)
            page = new Page<>();
        page.setRecords(users);
        return page;
    }

    public List<User> listByRoleId(int roleId, Wrapper wrapper) {
        return baseMapper.listByRoleId(roleId, wrapper);
    }

    /**
     * 当用户已存在，更新用户微信公众号openId
     * 用户不存在，则插入新用户
     */
    public int saveUserOrUpdateMpOpenIdOnDuplicateUnionId(String phone, String unionId, String maOpenId, String mpOpenId) {
        Assert.isTrue(StringUtils.isNotEmpty(phone) || StringUtils.isNotEmpty(unionId) || StringUtils.isNotEmpty(maOpenId) || StringUtils.isNotEmpty(mpOpenId), "参数不合法");
        return baseMapper.saveUserOnDuplicateKeyUpdateWxUserInfo(phone, unionId, maOpenId, mpOpenId);
    }

    /**
     * 根据id获取用户信息 不包含角色信息
     */
    public User selectUserById(Integer id) {
        User user = baseMapper.selectById(id);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChangeDoctor(ChangeDoctorDTO dto) {
        Integer userId = SecurityUtils.getUser().getId();
        hospitalDoctorRelationService.bind(dto.getHospitalId(), userId);

        Role role = roleService.getRoleByCode("ROLE_DOCTOR");
        if (role == null) throw new InnerException("医生权限不存在，无法授权");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleService.save(userRole);

        LambdaUpdateWrapper wrapper = new UpdateWrapper<User>().lambda()
                .set(User::getPhone, dto.getPhone())
                .set(User::getRealName, dto.getRealName())
                .set(User::getRemarks, dto.getRemarks());


    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChangeDoctor(User user) {

        User one = this.getBaseMapper().getPhoneIsExist(user.getPhone());
        if (one != null && !one.getId().equals(user.getId())) {
            throw new InnerException("手机号已被使用");
        }

        this.updateById(user);

        //为该用户添加医生权限
        userRoleService.remove(new QueryWrapper<UserRole>().lambda().eq(UserRole::getUserId, user.getId()).eq(UserRole::getRoleId, 20));
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(20);
        userRoleService.save(userRole);
        //为医生添加tag
        List<UserRole> userRoles = userRoleService.getListByUserId(user.getId());

        wxMpTagService.batchTaggings(userRoles, user.getId());


    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePwd(UserPwdDTO userPwdDTO) {
        User byId = this.getById(SecurityUtils.getUser().getId());
        if (byId == null)
            throw new InnerException("用户不存在");
        if (byId.getPassword() != null) {

            if (ENCODER.matches(byId.getPassword(), userPwdDTO.getOldPwd()))
                throw new InnerException("修改失败,旧密码错误");
        }
        User u = new User();
        u.setId(byId.getId());
        u.setPassword(ENCODER.encode(userPwdDTO.getNewPwd()));
        this.updateById(u);
    }
}
