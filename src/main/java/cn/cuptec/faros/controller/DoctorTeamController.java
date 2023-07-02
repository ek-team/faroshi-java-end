package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.BindDiseasesParam;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.UploadFileUtils;
import com.alibaba.excel.EasyExcel;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.jws.soap.SOAPBinding;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生团队管理
 */
@AllArgsConstructor
@RestController
@RequestMapping("/doctorTeam")
public class DoctorTeamController extends AbstractBaseController<DoctorTeamService, DoctorTeam> {
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private UserService userService;
    @Resource
    private DoctorTeamDiseasesService doctorTeamDiseasesService;//医生病种关联
    @Resource
    private DiseasesService diseasesService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private DoctorTeamDeptService doctorTeamDeptService;

    private final Url urlData;

    private final OssProperties ossProperties;
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();


    @PostMapping("/importDoctor")
    public RestResponse importDoctor(@RequestPart(value = "file") MultipartFile file) {
        User byId = userService.getById(SecurityUtils.getUser().getId());

        try {
            List<ImportDoctor> importDoctors = EasyExcel.read(file.getInputStream())
                    .head(ImportDoctor.class)
                    .sheet()
                    .doReadSync();
            if (!CollectionUtils.isEmpty(importDoctors)) {
                List<User> users = new ArrayList<>();
                List<HospitalInfo> hospitalInfos = new ArrayList<>();
                List<String> mobiles = new ArrayList<>();
                List<String> hospitalInfoStrs = new ArrayList<>();
                int size = 0;
                String teamPhone = "";
                String hospitalInfoStrThis = "";
                for (ImportDoctor importDoctor : importDoctors) {

                    String hospitalInfoStr = importDoctor.getProvince() + importDoctor.getCity() + importDoctor.getArea() + importDoctor.getHospital();

                    //判断医生是否存在
                    mobiles.add(importDoctor.getMobile());
                    User user = new User();
                    if (importDoctor.getRole().equals("组长")) {
                        user.setImportTeam("1");
                        teamPhone = importDoctor.getMobile();
                        user.setImportTeamPhone(teamPhone);
                        user.setHospitalInfoStr(hospitalInfoStr);
                        hospitalInfoStrThis = hospitalInfoStr;
                        size = 1;
                    } else {
                        size = 0;
                    }
                    if (size == 0) {
                        user.setImportTeamPhone(teamPhone);
                        user.setHospitalInfoStr(hospitalInfoStrThis);
                    }
                    user.setNickname(importDoctor.getUserName());
                    user.setDeptId(byId.getDeptId());
                    user.setPhone(importDoctor.getMobile());
                    user.setLever(importDoctor.getLevel());
                    user.setDepartment(importDoctor.getDepartment());
                    user.setPassword(ENCODER.encode(importDoctor.getMobile().substring(5, 11)));
                    users.add(user);

                    //判断医院是否存在
                    if (!hospitalInfoStrs.contains(hospitalInfoStr)) {
                        HospitalInfo hospitalInfo = new HospitalInfo();
                        hospitalInfo.setName(importDoctor.getHospital());
                        hospitalInfo.setArea(importDoctor.getArea());
                        hospitalInfo.setCity(importDoctor.getCity());
                        hospitalInfo.setProvince(importDoctor.getProvince());
                        hospitalInfo.setHospitalInfoStr(hospitalInfoStr);
                        hospitalInfo.setCreateTime(new Date());
                        hospitalInfos.add(hospitalInfo);
                    }
                    hospitalInfoStrs.add(hospitalInfoStr);
                }
                List<User> oldUsers = userService.list(new QueryWrapper<User>().lambda().in(User::getPhone, mobiles)
                );
                if (!CollectionUtils.isEmpty(oldUsers)) {
                    Map<String, User> oldUserMap = oldUsers.stream()
                            .collect(Collectors.toMap(User::getPhone, t -> t));
                    for (User user : users) {
                        User user1 = oldUserMap.get(user.getPhone());
                        if (user1 != null) {
                            user.setId(user1.getId());
                        }
                    }
                }
                userService.saveOrUpdateBatch(users);
                List<UserRole> userRoles = new ArrayList<>();
                List<Integer> userIds = new ArrayList<>();
                for (User user : users) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(20);
                    userRoles.add(userRole);
                    userIds.add(user.getId());
                }
                userRoleService.remove(new QueryWrapper<UserRole>().lambda().eq(UserRole::getRoleId, 20)
                        .in(UserRole::getUserId, userIds));
                userRoleService.saveOrUpdateBatch(userRoles);

                List<HospitalInfo> oldHospitalInfos = hospitalInfoService.list(new QueryWrapper<HospitalInfo>().lambda().in(HospitalInfo::getHospitalInfoStr, hospitalInfoStrs)
                );
                if (!CollectionUtils.isEmpty(oldHospitalInfos)) {
                    Map<String, HospitalInfo> oldHospitalInfoMap = oldHospitalInfos.stream()
                            .collect(Collectors.toMap(HospitalInfo::getHospitalInfoStr, t -> t));
                    for (HospitalInfo hospitalInfo : hospitalInfos) {
                        HospitalInfo ospitalInfo1 = oldHospitalInfoMap.get(hospitalInfo.getHospitalInfoStr());
                        if (ospitalInfo1 != null) {
                            hospitalInfo.setId(ospitalInfo1.getId());
                        }
                    }
                }
                hospitalInfoService.saveOrUpdateBatch(hospitalInfos);
                Map<String, HospitalInfo> hospitalInfoMap = hospitalInfos.stream()
                        .collect(Collectors.toMap(HospitalInfo::getHospitalInfoStr, t -> t));

                //创建团队
                Map<String, List<User>> userTeamMap = users.stream()
                        .collect(Collectors.groupingBy(User::getImportTeamPhone));
                List<DoctorTeam> doctorTeamList = new ArrayList<>();

                for (List<User> value : userTeamMap.values()) {
                    List<User> userList = value;
                    Integer leaderId = 0;
                    String name = "";
                    List<DoctorTeamPeople> doctorTeamPeopleList = new ArrayList<>();
                    for (User user : userList) {
                        DoctorTeamPeople doctorTeamPeople = new DoctorTeamPeople();
                        doctorTeamPeople.setUserId(user.getId());
                        doctorTeamPeopleList.add(doctorTeamPeople);
                        if (!StringUtils.isEmpty(user.getImportTeam())) {
                            leaderId = user.getId();
                            name = user.getNickname() + "团队";
                        }
                    }

                    DoctorTeam doctorTeam = new DoctorTeam();
                    doctorTeam.setDeptId(byId.getDeptId());
                    doctorTeam.setDeptIdList(byId.getDeptId() + "");
                    doctorTeam.setHospitalId(hospitalInfoMap.get(value.get(0).getHospitalInfoStr()).getId());
                    doctorTeam.setDoctorTeamPeopleList(doctorTeamPeopleList);
                    doctorTeam.setLeaderId(leaderId);
                    doctorTeam.setName(name);
                    doctorTeam.setStatus(1);
                    doctorTeam.setCreateUserId(byId.getId());
                    doctorTeam.setCreateTime(LocalDateTime.now());
                    doctorTeam.setModel(2);
                    doctorTeamList.add(doctorTeam);
                }

                for (DoctorTeam doctorTeam : doctorTeamList) {
                    this.add(doctorTeam);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return RestResponse.ok();
    }

    public static void main(String[] args) {
        String a = "13816810426";
        System.out.println(a.substring(5, 11));
    }

    /**
     * 添加医生团队
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody DoctorTeam doctorTeam) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        if (doctorTeam.getStatus() == null) {
            doctorTeam.setStatus(0);

        }
        doctorTeam.setDeptId(byId.getDeptId());
        doctorTeam.setDeptIdList(byId.getDeptId() + "");
        doctorTeam.setCreateTime(LocalDateTime.now());
        service.save(doctorTeam);
        //生成一个图片返回
        String url = urlData.getUrl() + "index.html#/newPlatform/addFriends?doctorId=" + doctorTeam.getId() + "-";
        BufferedImage png = null;
        try {
            png = QrCodeUtil.doctorImage(ServletUtils.getResponse().getOutputStream(), "", url, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = "";
        //转换上传到oss
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut = null;
        try {
            imOut = ImageIO.createImageOutputStream(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(png, "png", imOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
        try {
            OSS ossClient = UploadFileUtils.getOssClient(ossProperties);
            Random random = new Random();
            name = random.nextInt(10000) + System.currentTimeMillis() + "_YES.png";
            // 上传文件
            PutObjectResult putResult = ossClient.putObject(ossProperties.getBucket(), "poster/" + name, inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/avatar/1673835893578_b9f1ad25.png
        String resultStr = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/" + "poster/" + name;
        doctorTeam.setQrCode(resultStr);
        service.updateById(doctorTeam);

        Integer leaderId = doctorTeam.getLeaderId();

        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeam.getDoctorTeamPeopleList();
        if (leaderId != null) {
            DoctorTeamPeople doctorTeamPeople = new DoctorTeamPeople();
            doctorTeamPeople.setTeamId(doctorTeam.getId());
            doctorTeamPeople.setUserId(leaderId);
            doctorTeamPeopleList.add(doctorTeamPeople);
        }
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                doctorTeamPeople.setTeamId(doctorTeam.getId());
            }

        }
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            List<String> Ids = new ArrayList<>();//用来临时存储person的id


            doctorTeamPeopleList = doctorTeamPeopleList.stream().filter(// 过滤去重
                    v -> {
                        boolean flag = !Ids.contains(v.getUserId() + "");
                        Ids.add(v.getUserId() + "");
                        return flag;
                    }
            ).collect(Collectors.toList());

            doctorTeamPeopleService.saveBatch(doctorTeamPeopleList);
        }
        return RestResponse.ok();
    }

    /**
     * 判断医生是否是队长
     *
     * @return
     */
    @GetMapping("/checkLeader")
    public RestResponse checkLeader(@RequestParam("teamId") int teamId, @RequestParam("doctorId") int doctorId) {
        DoctorTeam doctorTeam = service.getById(teamId);
        if (doctorTeam.getLeaderId().equals(doctorId)) {
            return RestResponse.ok(true);
        }
        return RestResponse.ok(false);
    }

    /**
     * 判断医生是否是队长
     *
     * @return
     */
    @GetMapping("/getPLanCheckStatus")
    public RestResponse getPLanCheckStatis(@RequestParam("teamId") int teamId) {
        DoctorTeam doctorTeam = service.getById(teamId);

        return RestResponse.ok(doctorTeam.getPlanCheckStatus());
    }


    @GetMapping("/setPLanCheckStatus")
    public RestResponse setPLanCheckStatis(@RequestParam("teamId") int teamId, @RequestParam("status") int status) {
        DoctorTeam doctorTeam = service.getById(teamId);
        doctorTeam.setPlanCheckStatus(status);
        service.updateById(doctorTeam);
        return RestResponse.ok();
    }

    /**
     * 编辑医生团队
     *
     * @return
     */
    @PostMapping("/update")
    public RestResponse update(@RequestBody DoctorTeam doctorTeam) {
        DoctorTeam byId = service.getById(doctorTeam.getId());
        if (byId.getStatus().equals(2)) {
            doctorTeam.setStatus(0);
        }

        service.updateById(doctorTeam);
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, doctorTeam.getId()));
        List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeam.getDoctorTeamPeopleList();

        Integer leaderId = doctorTeam.getLeaderId();
        if (leaderId != null) {
            DoctorTeamPeople doctorTeamPeople = new DoctorTeamPeople();
            doctorTeamPeople.setTeamId(doctorTeam.getId());
            doctorTeamPeople.setUserId(leaderId);
            doctorTeamPeopleList.add(doctorTeamPeople);
        }
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                doctorTeamPeople.setTeamId(doctorTeam.getId());
            }
        }
        if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
            List<String> Ids = new ArrayList<>();//用来临时存储person的id
            doctorTeamPeopleList = doctorTeamPeopleList.stream().filter(// 过滤去重
                    v -> {
                        boolean flag = !Ids.contains(v.getUserId() + "");
                        Ids.add(v.getUserId() + "");
                        return flag;
                    }
            ).collect(Collectors.toList());

            doctorTeamPeopleService.saveBatch(doctorTeamPeopleList);
        }
        return RestResponse.ok();
    }

    /**
     * 删除医生团队
     *
     * @return
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") int id) {
        service.update(Wrappers.<DoctorTeam>lambdaUpdate()
                .eq(DoctorTeam::getId, id)
                .set(DoctorTeam::getDel, 1));

        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, id));
        return RestResponse.ok();
    }

    /**
     * 分页查询医生团队
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped(@RequestParam(required = false, value = "status") Integer status,
                                   @RequestParam(required = false, value = "hospitalName") String hospitalName,
                                   @RequestParam(required = false, value = "hospitalId") Integer hospitalId,
                                   @RequestParam(required = false, value = "doctorName") String doctorName) {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage<DoctorTeam> result = new Page<>();


        if (!StringUtils.isEmpty(doctorName)) {
            List<User> users = userService.list(new QueryWrapper<User>().lambda().eq(User::getNickname, doctorName));
            if (CollectionUtils.isEmpty(users)) {
                return RestResponse.ok(result);
            }
            List<Integer> userIds = users.stream().map(User::getId)
                    .collect(Collectors.toList());
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .in(DoctorTeamPeople::getUserId, userIds));
            if (CollectionUtils.isEmpty(doctorTeamPeopleList)) {

                return RestResponse.ok(result);
            }
            List<Integer> teamIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getTeamId)
                    .collect(Collectors.toList());
            queryWrapper.in("doctor_team.id", teamIds);

        }


        Page<DoctorTeam> page = getPage();
        if (!StringUtils.isEmpty(hospitalName)) {
            List<HospitalInfo> hospitalInfos = hospitalInfoService.list(new QueryWrapper<HospitalInfo>().lambda().eq(HospitalInfo::getName, hospitalName));
            if (CollectionUtils.isEmpty(hospitalInfos)) {
                return RestResponse.ok(result);
            }
            List<Integer> hospitalInfoIds = hospitalInfos.stream().map(HospitalInfo::getId)
                    .collect(Collectors.toList());
            queryWrapper.in("hospital_id", hospitalInfoIds);
        }

        if (hospitalId != null) {

            queryWrapper.eq("hospital_id", hospitalId);
        }
        User user = userService.getById(SecurityUtils.getUser().getId());

        if (status != null) {
            queryWrapper.eq("status", 1);
        }
        User userDept = userService.getById(SecurityUtils.getUser().getId());
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(userDept.getId());
        if (!aBoolean) {
            queryWrapper.like("doctor_team.dept_id_list", userDept.getDeptId());

        }
        queryWrapper.eq("del", 0);
        IPage<DoctorTeam> doctorTeamIPage = service.pageScoped(page, queryWrapper);
        return RestResponse.ok(doctorTeamIPage);
    }

    /**
     * 查询医生团队 只查询有 成员的的团队
     */
    @GetMapping("/pageScopedHavePeople")
    public RestResponse pageScopedHavePeople() {
        User user = userService.getById(SecurityUtils.getUser().getId());
        List<DoctorTeam> doctorTeams = service.pageScopedHavePeople(user.getDeptId() + "");
        doctorTeams = doctorTeams.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DoctorTeam::getId))), ArrayList::new)
        );

        return RestResponse.ok(doctorTeams);
    }

    /**
     * 根据id查询详情
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") int id) {
        DoctorTeam byId = service.getById(id);
        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, id));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> userIds = list.stream().map(DoctorTeamPeople::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (DoctorTeamPeople doctorTeamPeople : list) {
                doctorTeamPeople.setUserName(userMap.get(doctorTeamPeople.getUserId()).getNickname());
                doctorTeamPeople.setAvatar(userMap.get(doctorTeamPeople.getUserId()).getAvatar());

            }
        }
        byId.setDoctorTeamPeopleList(list);
        Integer leaderId = byId.getLeaderId();
        if (leaderId != null) {
            User leaderUser = userService.getById(leaderId);
            byId.setLeaderUser(leaderUser);
        }
        return RestResponse.ok(byId);
    }

    /**
     * 审核医生团队
     *
     * @return
     */
    @PostMapping("/checkDoctorTeam")
    public RestResponse checkDoctorTeam(@RequestBody DoctorTeam doctorTeam) {
        service.updateById(doctorTeam);
        return RestResponse.ok();
    }

    /**
     * 医生团队绑定病种
     *
     * @return
     */
    @PostMapping("/bindDiseases")
    public RestResponse bindDiseases(@RequestBody BindDiseasesParam param) {
        doctorTeamDiseasesService.remove(new QueryWrapper<DoctorTeamDiseases>().lambda()
                .eq(DoctorTeamDiseases::getTeamId, param.getTeamId()));
        List<Integer> diseasesIds = param.getDiseasesIds();
        if (!CollectionUtils.isEmpty(diseasesIds)) {
            List<DoctorTeamDiseases> doctorTeamDiseases = new ArrayList<>();
            for (Integer diseasesId : diseasesIds) {
                DoctorTeamDiseases diseases = new DoctorTeamDiseases();
                diseases.setDiseasesId(diseasesId);
                diseases.setTeamId(param.getTeamId());
                doctorTeamDiseases.add(diseases);
            }
            doctorTeamDiseasesService.saveBatch(doctorTeamDiseases);


        }
        return RestResponse.ok();
    }

    /**
     * 查询医生团队绑定的病种
     *
     * @return
     */
    @GetMapping("/getTeamDiseases")
    public RestResponse getTeamDiseases(@RequestParam("teamId") int teamId) {
        List<DoctorTeamDiseases> list = doctorTeamDiseasesService.list(new QueryWrapper<DoctorTeamDiseases>().lambda()
                .eq(DoctorTeamDiseases::getTeamId, teamId));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> diseasesIds = list.stream().map(DoctorTeamDiseases::getDiseasesId)
                    .collect(Collectors.toList());
            List<Diseases> diseases = (List<Diseases>) diseasesService.listByIds(diseasesIds);

            return RestResponse.ok(diseases);
        }
        return RestResponse.ok();
    }

    /**
     * 查询医生所在的团队
     *
     * @return
     */

    @GetMapping("/queryMyTeam")
    public RestResponse queryMyTeam() {
        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(new ArrayList<>());
        }
        List<Integer> teamIds = list.stream().map(DoctorTeamPeople::getTeamId)
                .collect(Collectors.toList());
        List<DoctorTeam> doctorTeams = (List<DoctorTeam>) service.listByIds(teamIds);

        List<Integer> hospitalIds = doctorTeams.stream().map(DoctorTeam::getHospitalId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(hospitalIds)) {
            List<HospitalInfo> hospitalInfos = (List<HospitalInfo>) hospitalInfoService.listByIds(hospitalIds);
            Map<Integer, HospitalInfo> hospitalInfoMap = hospitalInfos.stream()
                    .collect(Collectors.toMap(HospitalInfo::getId, t -> t));
            for (DoctorTeam doctorTeam : doctorTeams) {
                HospitalInfo hospitalInfo = hospitalInfoMap.get(doctorTeam.getHospitalId());
                if (hospitalInfo != null) {
                    doctorTeam.setHospitalName(hospitalInfo.getName());

                }
            }
        }
        return RestResponse.ok(doctorTeams);
    }

    /**
     * 查询医生所在的团队
     *
     * @return
     */

    @GetMapping("/queryMyTeamNoResultHospital")
    public RestResponse queryMyTeamNoResultHospital() {
        List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                .eq(DoctorTeamPeople::getUserId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(new ArrayList<>());
        }
        List<Integer> teamIds = list.stream().map(DoctorTeamPeople::getTeamId)
                .collect(Collectors.toList());
        List<DoctorTeam> doctorTeams = (List<DoctorTeam>) service.listByIds(teamIds);


        return RestResponse.ok(doctorTeams);
    }

    /**
     * 查询设备绑定团队
     *
     * @return
     */
    @GetMapping("/getByMacAdd")
    public RestResponse getByMacAdd(@RequestParam("macAdd") String macAdd) {
        ProductStock byMac = productStockService.getByMac(macAdd);
        String servicePackId = byMac.getServicePackId();
        if (StringUtils.isEmpty(servicePackId)) {
            return RestResponse.ok();
        }
        //查询服务信息
        List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>()
                .lambda().eq(ServicePackageInfo::getServicePackageId, servicePackId));
        //查询服务的医生团队
        List<DoctorTeam> doctorTeams = new ArrayList<>();
        List<Integer> teamIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(servicePackageInfos)) {

            for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                String doctorTeamId = servicePackageInfo.getDoctorTeamId();
                List<Integer> teamId = new ArrayList<>();
                if (!StringUtils.isEmpty(doctorTeamId)) {
                    String[] split = doctorTeamId.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String n = split[i];
                        teamIds.add(Integer.parseInt(n));
                        teamId.add(Integer.parseInt(n));
                    }
                }
                servicePackageInfo.setDoctorTeamIds(teamId);
            }
            if (!CollectionUtils.isEmpty(teamIds)) {
                doctorTeams = (List<DoctorTeam>) service.listByIds(teamIds);

            }

        }
        if (!CollectionUtils.isEmpty(doctorTeams)) {

            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .in(DoctorTeamPeople::getTeamId, teamIds));


            List<Integer> hospitalIds = doctorTeams.stream().map(DoctorTeam::getHospitalId)
                    .collect(Collectors.toList());
            List<HospitalInfo> hospitalInfos = (List<HospitalInfo>) hospitalInfoService.listByIds(hospitalIds);
            Map<Integer, HospitalInfo> hospitalInfoMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(hospitalInfos)) {
                hospitalInfoMap = hospitalInfos.stream()
                        .collect(Collectors.toMap(HospitalInfo::getId, t -> t));


            }

            Map<Integer, List<DoctorTeamPeople>> map = new HashMap<>();
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                List<User> users = (List<User>) userService.listByIds(userIds);
                Map<Integer, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
                for (DoctorTeamPeople doctorTeamPeople : doctorTeamPeopleList) {
                    if (userMap.get(doctorTeamPeople.getUserId()) != null) {
                        doctorTeamPeople.setUserName(userMap.get(doctorTeamPeople.getUserId()).getNickname());
                        doctorTeamPeople.setAvatar(userMap.get(doctorTeamPeople.getUserId()).getAvatar());

                    }
                }
                map = doctorTeamPeopleList.stream()
                        .collect(Collectors.groupingBy(DoctorTeamPeople::getTeamId));

            }

            for (DoctorTeam doctorTeam : doctorTeams) {
                doctorTeam.setDoctorTeamPeopleList(map.get(doctorTeam.getId()));
                doctorTeam.setHospitalInfo(hospitalInfoMap.get(doctorTeam.getHospitalId()));
            }
        }
        return RestResponse.ok(doctorTeams);
    }


    @Override
    protected Class<DoctorTeam> getEntityClass() {
        return DoctorTeam.class;
    }

}
