package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.BindDiseasesParam;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.UploadFileUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
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
    private final OssProperties ossProperties;

    /**
     * 添加医生团队
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody DoctorTeam doctorTeam) {
        User byId = userService.getById(SecurityUtils.getUser().getId());

        doctorTeam.setStatus(0);
        doctorTeam.setDeptId(byId.getDeptId());
        doctorTeam.setCreateTime(LocalDateTime.now());
        service.save(doctorTeam);
        //生成一个图片返回
        String url = "https://pharos3.ewj100.com/index.html#/newPlatform/addFriends?doctorId=" + doctorTeam.getId() + "-";
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
        //生成一个图片返回
        String url = "https://pharos3.ewj100.com/index.html#/newPlatform/addFriends?doctorId=" + doctorTeam.getId() + "-";
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
        service.removeById(id);
        doctorTeamPeopleService.remove(new QueryWrapper<DoctorTeamPeople>().lambda().eq(DoctorTeamPeople::getTeamId, id));
        return RestResponse.ok();
    }

    /**
     * 分页查询医生团队
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped(@RequestParam(required = false, value = "status") Integer status) {
        Page<DoctorTeam> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        User user = userService.getById(SecurityUtils.getUser().getId());
        DataScope dataScope = new DataScope();

        if (user.getDeptId().equals(1)) {
            dataScope.setIsOnly(false);
        } else {
            dataScope.setIsOnly(true);
        }
        if (status != null) {
            queryWrapper.eq("status", 1);
        }
        IPage<DoctorTeam> doctorTeamIPage = service.pageScoped(page, queryWrapper, dataScope);
        return RestResponse.ok(doctorTeamIPage);
    }

    /**
     * 查询医生团队 只查询有 成员的的团队
     */
    @GetMapping("/pageScopedHavePeople")
    public RestResponse pageScopedHavePeople() {
        User user = userService.getById(SecurityUtils.getUser().getId());
        List<DoctorTeam> doctorTeams = service.pageScopedHavePeople(user.getDeptId());
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

    @Override
    protected Class<DoctorTeam> getEntityClass() {
        return DoctorTeam.class;
    }

}
