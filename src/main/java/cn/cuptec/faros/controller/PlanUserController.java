package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.UserOrderMapper;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ExcelUtil;
import cn.cuptec.faros.util.IdCardUtil;
import cn.cuptec.faros.util.SnowflakeIdWorker;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.expr.Cast;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/palnUser")
public class PlanUserController extends AbstractBaseController<PlanUserService, TbTrainUser> {
    @Resource
    private ProductStockService productStockService;
    @Resource
    private UserOrderMapper userOrderMapper;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private NaLiUserInfoService naLiUserInfoService;
    @Resource
    private WxMpService wxMpService;
    @Resource
    private UserService userService;
    @Resource
    private LiveQrCodeService liveQrCodeService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private DeviceScanSignLogService deviceScanSignLogService;
    @Resource
    private MobileService mobileService;
    @Resource
    private CityService cityService;
    @Resource
    private UserQrCodeService userQrCodeService;
    @Resource
    private PlanUserBackupService planUserBackupService;
    @Resource
    private TbUserTrainRecordBackupService tbUserTrainRecordBackupService;
    @Resource
    private PlanUserTrainRecordService planUserTrainRecordService;
    @Resource
    private TbPlanBackupService tbPlanBackupService;
    @Resource
    private PlanService planService;
    @Resource
    private TbSubPlanBackupService tbSubPlanBackupService;
    @Resource
    private SubPlanService subPlanService;
    @Resource
    private TbTrainDataBackupService tbTrainDataBackupService;
    @Resource
    private TrainDataService trainDataService;
    @Resource
    private EvaluationRecordsBackupService evaluationRecordsBackupService;
    @Resource
    private EvaluationRecordsService evaluationRecordsService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private DoctorTeamService doctorTeamService;
    private final Url url;
    @Resource
    private OperationRecordService operationRecordService;

    @GetMapping("/page")
    public RestResponse pageList(@RequestParam(value = "maxAge", required = false) Integer maxAge, @RequestParam(value = "miniAge", required = false) Integer miniAge,
                                 @RequestParam(value = "maxHeight", required = false) Integer maxHeight, @RequestParam(value = "miniHeight", required = false) Integer miniHeight,
                                 @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate,
                                 @RequestParam(value = "startOnsetTime", required = false) String startOnsetTime, @RequestParam(value = "endOnsetTime", required = false) String endOnsetTime,
                                 @RequestParam(value = "miniEducationalLevel", required = false) String miniEducationalLevel, @RequestParam(value = "maxEducationalLevel", required = false) String maxEducationalLevel) {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<TbTrainUser> page = getPage();
        if (maxAge != null && miniAge != null) {
            queryWrapper.le("age", maxAge);
            queryWrapper.ge("age", miniAge);

        }
        if (maxHeight != null && miniHeight != null) {
            queryWrapper.le("CAST(height AS UNSIGNED)", maxHeight);//小于等于
            queryWrapper.ge("CAST(height AS UNSIGNED)", miniHeight);//大于等于

        }
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            queryWrapper.le("date", endDate);
            queryWrapper.ge("date", startDate);
        }
        if (!StringUtils.isEmpty(startOnsetTime) && !StringUtils.isEmpty(endOnsetTime)) {

            queryWrapper.le("onset_time", endOnsetTime);
            queryWrapper.ge("onset_time", startOnsetTime);
        }
        if (!StringUtils.isEmpty(maxEducationalLevel) && !StringUtils.isEmpty(miniEducationalLevel)) {
            List<String> educationalLevel = new ArrayList<>();
            educationalLevel.add("小学");
            educationalLevel.add("初中");
            educationalLevel.add("高中");
            educationalLevel.add("大学本科");
            educationalLevel.add("硕士");
            educationalLevel.add("博士");
            List<String> selectEducationalLevel = new ArrayList<>();
            int i = educationalLevel.indexOf(maxEducationalLevel);
            if (i >= 0) {
                for (int j = 0; j <= i; j++) {
                    selectEducationalLevel.add(educationalLevel.get(j));
                }
            }
            List<String> selectEducationalLevel1 = new ArrayList<>();
            int a = educationalLevel.indexOf(miniEducationalLevel);
            if (a >= 0) {
                for (int j = a; j < 6; j++) {
                    selectEducationalLevel1.add(educationalLevel.get(j));
                }
            }
            selectEducationalLevel1.retainAll(selectEducationalLevel);
            queryWrapper.in("educational_level", selectEducationalLevel1);
        }
        IPage page1 = service.page(page, queryWrapper);
        List<TbTrainUser> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> doctorTeamIdList = records.stream().map(TbTrainUser::getDoctorTeamId)
                    .collect(Collectors.toList());

            List<DoctorTeam> doctorTeams = (List<DoctorTeam>) doctorTeamService.listByIds(doctorTeamIdList);
            if (!CollectionUtils.isEmpty(doctorTeams)) {
                Map<Integer, DoctorTeam> teamMap = doctorTeams.stream()
                        .collect(Collectors.toMap(DoctorTeam::getId, t -> t));
                for (TbTrainUser tbTrainUser : records) {
                    if (tbTrainUser != null) {
                        DoctorTeam doctorTeam = teamMap.get(tbTrainUser.getDoctorTeamId());
                        if (doctorTeam != null) {
                            tbTrainUser.setDoctorTeam(doctorTeam.getName());

                        }
                    }

                }
                page1.setRecords(records);
            }

        }
        return RestResponse.ok(page1);
    }

    @GetMapping("/export")
    public RestResponse export(HttpServletResponse response, @RequestParam(value = "maxAge", required = false) Integer maxAge, @RequestParam(value = "miniAge", required = false) Integer miniAge,
                               @RequestParam(value = "maxHeight", required = false) Integer maxHeight, @RequestParam(value = "miniHeight", required = false) Integer miniHeight,
                               @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate,
                               @RequestParam(value = "startOnsetTime", required = false) String startOnsetTime, @RequestParam(value = "endOnsetTime", required = false) String endOnsetTime,
                               @RequestParam(value = "miniEducationalLevel", required = false) String miniEducationalLevel, @RequestParam(value = "maxEducationalLevel", required = false) String maxEducationalLevel) {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        if (maxAge != null && miniAge != null) {
            queryWrapper.le("age", maxAge);
            queryWrapper.ge("age", miniAge);

        }
        if (maxHeight != null && miniHeight != null) {
            queryWrapper.le("CAST(height AS UNSIGNED)", maxHeight);//小于等于
            queryWrapper.ge("CAST(height AS UNSIGNED)", miniHeight);//大于等于

        }
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            queryWrapper.le("date", endDate);
            queryWrapper.ge("date", startDate);
        }
        if (!StringUtils.isEmpty(startOnsetTime) && !StringUtils.isEmpty(endOnsetTime)) {

            queryWrapper.le("onset_time", endOnsetTime);
            queryWrapper.ge("onset_time", startOnsetTime);
        }
        if (!StringUtils.isEmpty(maxEducationalLevel) && !StringUtils.isEmpty(miniEducationalLevel)) {
            List<String> educationalLevel = new ArrayList<>();
            educationalLevel.add("小学");
            educationalLevel.add("初中");
            educationalLevel.add("高中");
            educationalLevel.add("大学本科");
            educationalLevel.add("硕士");
            educationalLevel.add("博士");
            List<String> selectEducationalLevel = new ArrayList<>();
            int i = educationalLevel.indexOf(maxEducationalLevel);
            if (i >= 0) {
                for (int j = 0; j <= i; j++) {
                    selectEducationalLevel.add(educationalLevel.get(j));
                }
            }
            List<String> selectEducationalLevel1 = new ArrayList<>();
            int a = educationalLevel.indexOf(miniEducationalLevel);
            if (a >= 0) {
                for (int j = a; j < 6; j++) {
                    selectEducationalLevel1.add(educationalLevel.get(j));
                }
            }
            selectEducationalLevel1.retainAll(selectEducationalLevel);
            queryWrapper.in("educational_level", selectEducationalLevel1);
        }
        List<TbTrainUser> tbTrainUsers = service.list(queryWrapper);
        if (!CollectionUtils.isEmpty(tbTrainUsers)) {
            SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
            List<PlanUserExcel> planUserExcelList = new ArrayList<>();
            for (TbTrainUser tbTrainUser : tbTrainUsers) {
                PlanUserExcel planUserExcel = new PlanUserExcel();
                planUserExcel.setAddress(tbTrainUser.getAddress());
                if (tbTrainUser.getDate() != null) {
                    String format = sdf.format(tbTrainUser.getDate());
                    planUserExcel.setDate(format);
                } else {
                    planUserExcel.setDate("");

                }

                planUserExcel.setDiagnosis(tbTrainUser.getDiagnosis());
                planUserExcel.setHospitalName(tbTrainUser.getHospitalName());
                planUserExcel.setIdCard(tbTrainUser.getIdCard());
                planUserExcel.setName(tbTrainUser.getName());
                planUserExcel.setWeight(tbTrainUser.getWeight());
                planUserExcel.setTelePhone(tbTrainUser.getTelePhone());
                planUserExcel.setAge("" + tbTrainUser.getAge());
                if (tbTrainUser.getIsTestAccount().equals(1)) {
                    planUserExcel.setIsTestAccount("是");

                } else {
                    planUserExcel.setIsTestAccount("否");

                }
                planUserExcel.setWeight(tbTrainUser.getWeight());

                planUserExcelList.add(planUserExcel);
            }
            try {
                String cFileName = URLEncoder.encode("planUser", "UTF-8");

                ExcelUtil.writeUserOrderExcel(response, planUserExcelList, cFileName, "planUser", PlanUserExcel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return RestResponse.ok();
    }

    /**
     * 根据mac地址查询设备用户
     */
    @GetMapping("/getPlanUserByMacAdd")
    public RestResponse getPlanUserByMacAdd(@RequestParam("macAdd") String macAdd) {
        List<TbTrainUser> list = service.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getMacAdd, macAdd));
        return RestResponse.ok(list);
    }

    /**
     * 根据序列号查询使用用户列表
     */
    @GetMapping("/getUsePlanUserByProductSn")
    public RestResponse getUsePlanUserByMacAdd(@RequestParam("productSn") String productSn) {
        List<TbUserTrainRecord> list = planUserTrainRecordService.list(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getProductSn, productSn));

        if (!CollectionUtils.isEmpty(list)) {
            Map<String, List<TbUserTrainRecord>> map = list.stream()
                    .collect(Collectors.groupingBy(TbUserTrainRecord::getUserId));

            List<String> userIds = list.stream().map(TbUserTrainRecord::getUserId)
                    .collect(Collectors.toList());
            List<TbTrainUser> tbTrainUsers = (List<TbTrainUser>) service.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getUserId, userIds));
            if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                for (TbTrainUser tbTrainUser : tbTrainUsers) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String d = format.format(map.get(tbTrainUser.getUserId()).get(0).getCreateDate());
                    try {
                        tbTrainUser.setUseTime(format.parse(d));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            return RestResponse.ok(tbTrainUsers);
        }

        return RestResponse.ok();
    }


    /**
     * 根据名称搜索微信用户
     *
     * @return
     */
    @GetMapping("/searchByDeviceUserName")
    public RestResponse searchByDeviceUserName(@RequestParam("deviceUsername") String deviceUsername) {
        List<TbTrainUser> list = service.list(new QueryWrapper<TbTrainUser>().lambda().
                like(TbTrainUser::getName, deviceUsername));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.failed("未查询到设备用户");
        }
        List<Integer> xtUserIds = new ArrayList<>();
        for (TbTrainUser tbTrainUser : list) {
            if (tbTrainUser.getXtUserId() != null) {
                xtUserIds.add(tbTrainUser.getXtUserId());
            }
        }
        if (CollectionUtils.isEmpty(xtUserIds)) {
            return RestResponse.ok(new ArrayList<>());
        }
        return RestResponse.ok(userService.listByIds(xtUserIds));
    }

    /**
     * 查看用户二维码
     *
     * @return
     */
    @GetMapping("/getQrCode")
    public RestResponse getQrCode(@RequestParam("userId") String userId) {
        String url = "https://pharos3.ewj100.com/user/srBindAdress/" + userId;
        BufferedImage png = null;
        try {
            png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", "", url, "", 300, "", 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
    }

    /**
     * 查看注册设备用户二维码
     *
     * @return
     */
    @GetMapping("/getRegisterPlanUserQrCode")
    public RestResponse getRegisterPlanUserQrCode(@RequestParam("macAdd") String macAdd) {
        String url = "https://pharos3.ewj100.com/palnUser/registerPlanUser/" + macAdd;
        BufferedImage png = null;
        try {
            png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", "", url, "", 300, "", 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
    }

    /**
     * 重定向 注册设备用户界面
     *
     * @param response
     */
    @SneakyThrows
    @GetMapping("/registerPlanUser/{macAdd}")
    public void registerPlanUser(@PathVariable String macAdd, HttpServletResponse response) {
        response.sendRedirect(url.getUrl() + QrCodeConstants.REGISTER_PLAN_USER_URL + "?macAdd=" + macAdd);
    }

    /**
     * 发送注册页面 和设备二维码页面 模版消息
     *
     * @return
     */
    @GetMapping("/sendRegisterNotice")
    public void sendRegisterNotice(@RequestParam("macAdd") String macAdd, @RequestParam("token") String token) {
        User user = userService.getById(SecurityUtils.getUser().getId());
        if (user != null) {
            if (!org.apache.commons.lang3.StringUtils.isEmpty(user.getMpOpenId())) {
                List<ProductStock> list = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                        .eq(ProductStock::getMacAddress, macAdd)
                        .eq(ProductStock::getDel, 1));
                ProductStock productStock = list.get(0);
                if (productStock.getServicePackId() != null) {
                    ServicePack servicePack = servicePackService.getById(productStock.getServicePackId());
                    String servicePackName = servicePack.getName();
                    wxMpService.sendSubNotice(user.getMpOpenId(), "扫码成功", servicePackName, "法罗适",
                            "点击查看详情", "/pages/goodsDetail/goodsDetail?id=" + servicePack.getId() + "&token=" + token);

                }

            }
        }
    }


    @PostMapping("/add")
    public RestResponse add(@RequestBody TbTrainUser tbTrainUser) {
        if (tbTrainUser.getCardType() != null && tbTrainUser.getCardType().equals(1)) {
            if (!StringUtils.isEmpty(tbTrainUser.getIdCard())) {

                if (!IdCardUtil.isValidCard(tbTrainUser.getIdCard())) {
                    return RestResponse.failed("身份证有误");
                }

            }
        }

        if (StringUtils.isEmpty(tbTrainUser.getIdCard()) && !StringUtils.isEmpty(tbTrainUser.getCaseHistoryNo())) {
            tbTrainUser.setIdCard(tbTrainUser.getCaseHistoryNo());
        }
        Integer id;
        if (SecurityUtils.getUser() == null) {
            id = tbTrainUser.getXtUserId();
        } else {
            id = SecurityUtils.getUser().getId();
        }

        List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().
                lambda().eq(ProductStock::getMacAddress, tbTrainUser.getMacAdd()).eq(ProductStock::getDel, 1));

        User user = new User();
        user.setId(id);
        user.setMacAdd(tbTrainUser.getMacAdd());
        if (!CollectionUtils.isEmpty(productStocks)) {
            user.setProductStockId(productStocks.get(0).getId());
            tbTrainUser.setRegisterProductSn(productStocks.get(0).getProductSn());
        }

        userService.updateById(user);
        tbTrainUser.setAge(countAge(tbTrainUser.getIdCard()));

        tbTrainUser.setCaseHistoryNo(tbTrainUser.getIdCard());
        tbTrainUser.setXtUserId(id);
        SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0, 0);
        long userId = idUtil.nextId();
        long keyId = idUtil.nextId();
        tbTrainUser.setUserId(userId + "");
        tbTrainUser.setKeyId(keyId);
        //如果身份证号存在覆盖
        List<TbTrainUser> list = service.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, tbTrainUser.getIdCard()));
        if (!CollectionUtils.isEmpty(list)) {
            if (tbTrainUser.getAccountStatus() == null) {
                return RestResponse.ok("1235", list.get(0)); //提示用户身份证已存在 使用新账号还是旧账号
            }
            for (TbTrainUser tbTrainUser1 : list) {
                tbTrainUser1.setXtUserId(null);
            }
            service.updateBatchById(list);
            if (tbTrainUser.getAccountStatus().equals(0)) {//新账号
                for (TbTrainUser tbTrainUser1 : list) {
                    tbTrainUser1.setIdCard("被覆盖" + tbTrainUser1.getIdCard());
                }
                service.updateBatchById(list);


                List<TbTrainUser> xtUserId = service.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getXtUserId, id));
                if (!CollectionUtils.isEmpty(xtUserId)) {
                    //将所有绑定的XtuserId 修改为null
                    for (TbTrainUser tbTrainUser1 : xtUserId) {
                        tbTrainUser1.setXtUserId(null);
                    }
                    service.updateBatchById(xtUserId);
                }
                tbTrainUser.setCreateDate(new Date());
                tbTrainUser.setUpdateDate(new Date());
                service.save(tbTrainUser);
            } else {
                //旧账号
                //如果身份证号存在 则和当前微信用户绑定 并返回 添加到扫描列表
                TbTrainUser tbTrainUser1 = list.get(0);
                tbTrainUser1.setMacAdd(tbTrainUser.getMacAdd());
                tbTrainUser1.setRegisterProductSn(tbTrainUser.getRegisterProductSn());
                tbTrainUser1.setXtUserId(id);
                service.updateById(tbTrainUser1);
                tbTrainUser = tbTrainUser1;
            }

            //添加到扫描列表
            deviceScanSignLogService.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, tbTrainUser.getMacAdd()).eq(DeviceScanSignLog::getUserId, userId));
            DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

            deviceScanSignLog.setUserId(userId + "");

            deviceScanSignLog.setMacAddress(tbTrainUser.getMacAdd());

            deviceScanSignLogService.save(deviceScanSignLog);
            return RestResponse.ok(tbTrainUser);
        }
        List<TbTrainUser> xtUserId = service.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getXtUserId, id));
        if (!CollectionUtils.isEmpty(xtUserId)) {
            //将所有绑定的XtuserId 修改为null
            for (TbTrainUser tbTrainUser1 : xtUserId) {
                tbTrainUser1.setXtUserId(null);
            }
            service.updateBatchById(xtUserId);
        }
        tbTrainUser.setCreateDate(new Date());
        tbTrainUser.setUpdateDate(new Date());
        service.save(tbTrainUser);
        List<TbTrainUser> userBeanList = new ArrayList<>();
        userBeanList.add(tbTrainUser);

        //添加到扫描列表
        deviceScanSignLogService.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, tbTrainUser.getMacAdd()).eq(DeviceScanSignLog::getUserId, userId));
        DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

        deviceScanSignLog.setUserId(userId + "");

        deviceScanSignLog.setMacAddress(tbTrainUser.getMacAdd());

        deviceScanSignLogService.save(deviceScanSignLog);
        return RestResponse.ok(tbTrainUser);
    }


    /**
     * 根据身份证的号码算出当前身份证持有者的年龄
     *
     * @return
     */
    public static int countAge(String idNumber) {
        if (idNumber.length() != 18 && idNumber.length() != 15) {
            return 0;
        }
        String year;
        String yue;
        String day;
        if (idNumber.length() == 18) {
            year = idNumber.substring(6).substring(0, 4);// 得到年份
            yue = idNumber.substring(10).substring(0, 2);// 得到月份
            day = idNumber.substring(12).substring(0, 2);//得到日
        } else {
            year = "19" + idNumber.substring(6, 8);// 年份
            yue = idNumber.substring(8, 10);// 月份
            day = idNumber.substring(10, 12);//日
        }
        Date date = new Date();// 得到当前的系统时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String fyear = format.format(date).substring(0, 4);// 当前年份
        String fyue = format.format(date).substring(5, 7);// 月份
        String fday = format.format(date).substring(8, 10);//
        int age = 0;
        if (Integer.parseInt(yue) == Integer.parseInt(fyue)) {//如果月份相同
            if (Integer.parseInt(day) <= Integer.parseInt(fday)) {//说明已经过了生日或者今天是生日
                age = Integer.parseInt(fyear) - Integer.parseInt(year);
            }
        } else {

            if (Integer.parseInt(yue) < Integer.parseInt(fyue)) {
                //如果当前月份大于出生月份
                age = Integer.parseInt(fyear) - Integer.parseInt(year);
            } else {
                //如果当前月份小于出生月份,说明生日还没过
                age = Integer.parseInt(fyear) - Integer.parseInt(year) - 1;
            }
        }
        System.out.println("age = " + age);
        return age;
    }

    @PostMapping("/saveBatch")
    public RestResponse<TbTrainUser> addSaveBatch(@RequestBody List<TbTrainUser> userBeanList) {
        if (CollUtil.isNotEmpty(userBeanList)) {
            service.batchSaveOrUpdate(userBeanList);
        }

        return RestResponse.ok();
    }

    @PostMapping("/newSaveBatch")
    public RestResponse<TbTrainUser> newSaveBatch(@RequestBody List<TbTrainUser> userBeanList) {
        if (CollUtil.isNotEmpty(userBeanList)) {
            return RestResponse.ok(service.newSaveBatch(userBeanList));

        }
        return RestResponse.failed("参数为空");

    }

    /**
     * 获取所有属性，包括父类
     *
     * @param object
     * @return
     */
    public static Field[] getAllFields(Object object) {
        Class clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public static StringBuilder compareContract(TbTrainUser sign, TbTrainUser existSign) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Field[] fields = getAllFields(sign);
            for (int j = 0; j < fields.length; j++) {
                Field field = fields[j];
                field.setAccessible(true);
                // 字段值
                if (field.get(sign) != null && fields[j].get(existSign) != null && !field.get(sign).equals(fields[j].get(existSign))) {
                    System.out.println(fields[j].get(existSign));
                    System.out.println(field.get(sign));
                    stringBuilder.append(fields[j].getName() + "-");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    public static void main(String[] args) {
        TbTrainUser tbTrainUser = new TbTrainUser();
        tbTrainUser.setAccount("asdasd");
        tbTrainUser.setIdCard("111111");
        TbTrainUser tbTrainUser1 = new TbTrainUser();
        tbTrainUser1.setAccount("asdasdasd");
        tbTrainUser1.setIdCard("1111");
        StringBuilder stringBuilder = compareContract(tbTrainUser, tbTrainUser1);
        String s = stringBuilder.toString();
        if(!StringUtils.isEmpty(s)){
            List<String> split = Arrays.asList(s.split("-"));



        }

        System.out.println();
    }

    @PostMapping("/updateById")
    public RestResponse<TbTrainUser> addSaveBatch(@RequestBody TbTrainUser tbTrainUser) {
        TbTrainUser byId = service.getById(tbTrainUser.getId());
        tbTrainUser.setXtUserId(byId.getXtUserId());
        service.updateById(tbTrainUser);
        //添加修记录
//        OperationRecord operationRecord = new OperationRecord();
//        operationRecord.setCreateTime(new Date());
//        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
//        operationRecord.setStr(tbTrainUser.getUserId());
//        operationRecord.setType(2);
//        //判断修改了哪些字段
//        if (byId.getIdCard().equals(tbTrainUser.getIdCard())) {
//
//        }
//        String oldText = "";
//        String newText = "";
//        operationRecord.setText("从" + oldText + "修改为" + newText);
//        operationRecordService.save(operationRecord);

        return RestResponse.ok();
    }

    @PostMapping("/updateByUserId")
    public RestResponse<TbTrainUser> updateByUserId(@RequestBody TbTrainUser tbTrainUser) {
        service.update(Wrappers.<TbTrainUser>lambdaUpdate()
                .eq(TbTrainUser::getUserId, tbTrainUser.getUserId())
                .set(TbTrainUser::getRecoveryPlanCleanTag, 0));
        return RestResponse.ok();
    }

    @GetMapping("/getById/{uid}")
    public RestResponse getByUId(@PathVariable long uid) {

        return RestResponse.ok(service.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getUserId, uid)));
    }

    @GetMapping("/getByXtUserId")
    public RestResponse getByXtUserId(@RequestParam(value = "xtUserId", required = false) Integer xtUserId) {
        if (xtUserId == null && SecurityUtils.getUser() != null) {
            xtUserId = SecurityUtils.getUser().getId();
        }
        return RestResponse.ok(service.getInfoByUXtUserId(xtUserId));
    }

    @GetMapping("/getByPhoneAndIdCard")
    public RestResponse getByPhoneAndIdCard(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "idCard", required = false) String idCard) {

        return RestResponse.ok(service.getInfoByPhoneAndIdCard(phone, idCard, null));
    }


    @PutMapping("/bindSystemUserId")
    public RestResponse bindSystemUserId(@RequestParam long uid, @RequestParam(value = "macAddress", required = false) String macAdd) {
        log.info("bindSystemUserId=======" + macAdd);
        service.bindSystemUserId(uid, macAdd);
//        System.out.println("zxczxczxc");
        return RestResponse.ok();
    }

    @PutMapping("/removeBindSystemUserId")
    public RestResponse removeBindSystemUserId() {
        LambdaUpdateWrapper wrapper = new UpdateWrapper<TbTrainUser>().lambda()
                .set(TbTrainUser::getXtUserId, null)
                .eq(TbTrainUser::getXtUserId, SecurityUtils.getUser().getId());
        service.update(wrapper);
//        System.out.println("zxczxczxc");
        return RestResponse.ok();
    }

    /**
     * 根据userId查询用户信息
     *
     * @return
     */
    @GetMapping("/getUserInfoByUserId")
    public RestResponse getUserInfoByUserId(@RequestParam("userId") String userId) {
        TbTrainUser tbTrainUser = service.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));
        return RestResponse.ok(tbTrainUser);
    }

    @GetMapping("getPageUserByDoctorId")
    public RestResponse getPageUserByDoctorId() {
        Page<TbTrainUser> page = getPage();
        return RestResponse.ok(service.getPageUserByDoctorId(page, SecurityUtils.getUser().getId(), null));
    }

    @GetMapping("getPageUserByHospitalId")
    public RestResponse getPageUserByHospitalId(@RequestParam("hospitalId") Integer hospitalId) {
        Page<TbTrainUser> page = getPage();
        return RestResponse.ok(service.getPageUserByDoctorId(page, SecurityUtils.getUser().getId(), hospitalId));
    }

    @Override
    protected Class<TbTrainUser> getEntityClass() {
        return TbTrainUser.class;
    }


    /**
     * 清空用户和设备的绑定
     */

    @GetMapping("/cleanBindInfo")
    public RestResponse cleanBindInfo(@RequestParam("macAdd") String macAdd) {
        LambdaUpdateWrapper wrapper = new UpdateWrapper<TbTrainUser>().lambda()
                .set(TbTrainUser::getMacAdd, "")
                .eq(TbTrainUser::getMacAdd, macAdd);
        service.update(wrapper);
        deviceScanSignLogService.remove(new UpdateWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAdd));

        return RestResponse.ok();
    }


    /**
     * 用户登陆成功之后修改绑定的设备信息
     */

    @GetMapping("/checkIdCard")
    public RestResponse checkIdCard(@RequestParam("userId") Long userId, @RequestParam("macAdd") String macAdd) {
        return RestResponse.ok();
//        ProductStock productStock = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAdd));
//        if (productStock == null) {
//            return RestResponse.failed();
//        }
////        List<NaLiUserInfo> list = naLiUserInfoService.list(new QueryWrapper<NaLiUserInfo>().lambda().eq(NaLiUserInfo::getProductSn, productStock.getProductSn()).orderByDesc(NaLiUserInfo::getCreateTime));
////        if (CollectionUtils.isEmpty(list)) {
////            return RestResponse.failed();
////        }
////        NaLiUserInfo naLiUserInfo = list.get(0);
//        TbTrainUser tbTrainUser = service.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));
//        if (tbTrainUser == null) {
//            return RestResponse.failed();
//        }
////        if (StringUtils.isEmpty(tbTrainUser.getIdCard())) {
////            return RestResponse.failed();
////        }
//        if (!productStock.getIccId().equals(tbTrainUser.getIdCard())) {
//            return RestResponse.failed();
//        }
//        return RestResponse.ok();
    }

    /**
     * 备份用户基本数据 和训练数据
     */
    @PostMapping("/backupInfo")
    public RestResponse backupInfo(@RequestBody TbTrainUser userBeanList) {
        String userId = userBeanList.getUserId();
        //备份用户基本信息
        TbTrainUser one = service.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));
        TbTrainUserBackup tbTrainUserBackup = new TbTrainUserBackup();
        BeanUtils.copyProperties(one, tbTrainUserBackup, "id");
        List<TbTrainUserBackup> tbTrainUserBackups = planUserBackupService.list(new QueryWrapper<TbTrainUserBackup>().lambda().eq(TbTrainUserBackup::getUserId, userId).orderByDesc(TbTrainUserBackup::getBackVersion));
        if (!CollectionUtils.isEmpty(tbTrainUserBackups)) {
            tbTrainUserBackup.setBackVersion(tbTrainUserBackups.get(0).getBackVersion() + 1);
        }
        planUserBackupService.save(tbTrainUserBackup);
        userBeanList.setId(one.getId());
        service.updateById(userBeanList);

        //备份用户 训练计划
        List<TbUserTrainRecord> list = planUserTrainRecordService.list(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getUserId, userId));
        if (!CollectionUtils.isEmpty(list)) {
            List<TbUserTrainRecordBackup> tbUserTrainRecordBackups = new ArrayList<>();
            List<TbUserTrainRecordBackup> beforeTbUserTrainRecordBackups = tbUserTrainRecordBackupService.list(new QueryWrapper<TbUserTrainRecordBackup>().lambda().eq(TbUserTrainRecordBackup::getUserId, userId).orderByDesc(TbUserTrainRecordBackup::getBackVersion));
            if (!CollectionUtils.isEmpty(beforeTbUserTrainRecordBackups)) {
                Integer version = beforeTbUserTrainRecordBackups.get(0).getBackVersion();
                for (TbUserTrainRecord tbUserTrainRecord : list) {
                    TbUserTrainRecordBackup tbUserTrainRecordBackup = new TbUserTrainRecordBackup();
                    BeanUtils.copyProperties(tbUserTrainRecord, tbUserTrainRecordBackup, "id");
                    tbUserTrainRecordBackup.setBackVersion(version + 1);
                    tbUserTrainRecordBackup.setOldId(tbUserTrainRecord.getId());
                    tbUserTrainRecordBackups.add(tbUserTrainRecordBackup);
                }
            } else {
                for (TbUserTrainRecord tbUserTrainRecord : list) {
                    TbUserTrainRecordBackup tbUserTrainRecordBackup = new TbUserTrainRecordBackup();
                    BeanUtils.copyProperties(tbUserTrainRecord, tbUserTrainRecordBackup, "id");
                    tbUserTrainRecordBackup.setOldId(tbUserTrainRecord.getId());
                    tbUserTrainRecordBackups.add(tbUserTrainRecordBackup);
                }
            }
            tbUserTrainRecordBackupService.saveBatch(tbUserTrainRecordBackups);
            //清空原先的数据
            planUserTrainRecordService.remove(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getUserId, userId));
        }

        //备份用户训练数据
        List<TbPlan> tbPlanList = planService.list(new QueryWrapper<TbPlan>().lambda().eq(TbPlan::getUserId, userId));

        if (!CollectionUtils.isEmpty(tbPlanList)) {
            List<TbPlanBackup> tbPlanBackups = new ArrayList<>();
            List<TbPlanBackup> beforeTbPlanBackups = tbPlanBackupService.list(new QueryWrapper<TbPlanBackup>().lambda().eq(TbPlanBackup::getUserId, userId).orderByDesc(TbPlanBackup::getBackVersion));

            if (!CollectionUtils.isEmpty(beforeTbPlanBackups)) {
                Integer version = beforeTbPlanBackups.get(0).getBackVersion();
                for (TbPlan tbPlan : tbPlanList) {
                    TbPlanBackup tbPlanBackup = new TbPlanBackup();
                    BeanUtils.copyProperties(tbPlan, tbPlanBackup, "id");
                    tbPlanBackup.setBackVersion(version + 1);
                    tbPlanBackups.add(tbPlanBackup);
                }
            } else {
                for (TbPlan tbPlan : tbPlanList) {
                    TbPlanBackup tbPlanBackup = new TbPlanBackup();
                    BeanUtils.copyProperties(tbPlan, tbPlanBackup, "id");
                    tbPlanBackups.add(tbPlanBackup);
                }
            }
            tbPlanBackupService.saveBatch(tbPlanBackups);
            planService.remove(new QueryWrapper<TbPlan>().lambda().eq(TbPlan::getUserId, userId));
        }

        List<TbSubPlan> tbSubPlans = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, userId));
        if (!CollectionUtils.isEmpty(tbSubPlans)) {
            List<TbSubPlanBackup> tbSubPlanBackups = new ArrayList<>();
            List<TbSubPlanBackup> beforeTbSubPlanBackups = tbSubPlanBackupService.list(new QueryWrapper<TbSubPlanBackup>().lambda().eq(TbSubPlanBackup::getUserId, userId).orderByDesc(TbSubPlanBackup::getVersion));
            if (!CollectionUtils.isEmpty(beforeTbSubPlanBackups)) {
                Integer version = beforeTbSubPlanBackups.get(0).getVersion();
                for (TbSubPlan tbSubPlan : tbSubPlans) {
                    TbSubPlanBackup tbSubPlanBackup = new TbSubPlanBackup();
                    BeanUtils.copyProperties(tbSubPlan, tbSubPlanBackup, "id");
                    tbSubPlanBackup.setVersion(version + 1);
                    tbSubPlanBackups.add(tbSubPlanBackup);
                }
            } else {
                for (TbSubPlan tbSubPlan : tbSubPlans) {
                    TbSubPlanBackup tbSubPlanBackup = new TbSubPlanBackup();
                    BeanUtils.copyProperties(tbSubPlan, tbSubPlanBackup, "id");
                    tbSubPlanBackups.add(tbSubPlanBackup);
                }
            }

            tbSubPlanBackupService.saveBatch(tbSubPlanBackups);
            subPlanService.remove(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, userId));
        }

        List<TbTrainData> tbTrainDatas = trainDataService.list(new QueryWrapper<TbTrainData>().lambda().eq(TbTrainData::getUserId, userId));
        if (!CollectionUtils.isEmpty(tbTrainDatas)) {
            List<TbTrainDataBackup> brforeTbTrainDataBackups = tbTrainDataBackupService.list(new QueryWrapper<TbTrainDataBackup>().lambda().eq(TbTrainDataBackup::getUserId, userId).orderByDesc(TbTrainDataBackup::getBackVersion));
            List<TbTrainDataBackup> tbTrainDataBackups = new ArrayList<>();
            if (!CollectionUtils.isEmpty(brforeTbTrainDataBackups)) {
                Integer version = brforeTbTrainDataBackups.get(0).getBackVersion();
                for (TbTrainData tbTrainData : tbTrainDatas) {
                    TbTrainDataBackup tbTrainDataBackup = new TbTrainDataBackup();
                    BeanUtils.copyProperties(tbTrainData, tbTrainDataBackup, "id");
                    tbTrainDataBackup.setBackVersion(version + 1);
                    tbTrainDataBackups.add(tbTrainDataBackup);
                }
            } else {
                for (TbTrainData tbTrainData : tbTrainDatas) {
                    TbTrainDataBackup tbTrainDataBackup = new TbTrainDataBackup();
                    BeanUtils.copyProperties(tbTrainData, tbTrainDataBackup, "id");
                    tbTrainDataBackups.add(tbTrainDataBackup);
                }

            }
            tbTrainDataBackupService.saveBatch(tbTrainDataBackups);
            trainDataService.remove(new QueryWrapper<TbTrainData>().lambda().eq(TbTrainData::getUserId, userId));
        }
        List<EvaluationRecords> evaluationRecords = evaluationRecordsService.list(new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, userId));
        if (!CollectionUtils.isEmpty(evaluationRecords)) {
            List<EvaluationRecordsBackup> beforeEvaluationRecordsBackups = evaluationRecordsBackupService.list(new QueryWrapper<EvaluationRecordsBackup>().lambda().eq(EvaluationRecordsBackup::getUserId, userId).orderByDesc(EvaluationRecordsBackup::getBackVersion));
            List<EvaluationRecordsBackup> evaluationRecordsBackups = new ArrayList<>();
            if (!CollectionUtils.isEmpty(beforeEvaluationRecordsBackups)) {
                Integer version = beforeEvaluationRecordsBackups.get(0).getBackVersion();
                for (EvaluationRecords evaluationRecord : evaluationRecords) {
                    EvaluationRecordsBackup evaluationRecordsBackup = new EvaluationRecordsBackup();
                    BeanUtils.copyProperties(evaluationRecord, evaluationRecordsBackup, "id");
                    evaluationRecordsBackup.setBackVersion(version + 1);
                    evaluationRecordsBackups.add(evaluationRecordsBackup);
                }
            } else {
                for (EvaluationRecords evaluationRecord : evaluationRecords) {
                    EvaluationRecordsBackup evaluationRecordsBackup = new EvaluationRecordsBackup();
                    BeanUtils.copyProperties(evaluationRecord, evaluationRecordsBackup, "id");
                    evaluationRecordsBackups.add(evaluationRecordsBackup);
                }
            }
            evaluationRecordsBackupService.saveBatch(evaluationRecordsBackups);
        }
        return RestResponse.ok();
    }

}
