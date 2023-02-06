package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.DoctorPointCountResult;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.pay.PayResultData;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 医生积分管理
 */
@RestController
@RequestMapping("/doctorPoint")
public class DoctorPointController extends AbstractBaseController<DoctorPointService, DoctorPoint> {
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    @Resource
    private DoctorUserActionService doctorUserActionService;//医生设置的服务价格
    @Resource
    private UserService userService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private WxPayFarosService wxPayFarosService;

    /**
     * 分页查询医生积分
     *
     * @return
     */
    @GetMapping("/getDoctorPoint")
    public RestResponse getDoctorPoint() {
        Page<DoctorPoint> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        queryWrapper.eq("doctor_user_id", SecurityUtils.getUser().getId());
        queryWrapper.orderByDesc("create_time", "withdraw_status");
        IPage<DoctorPoint> doctorPointIPage = service.page(page, queryWrapper);
        return RestResponse.ok(doctorPointIPage);
    }

    /**
     * 查询医生总积分
     *
     * @return
     */
    @GetMapping("/getDoctorTotalPoint")
    public RestResponse getDoctorTotalPoint() {
        DoctorPointCountResult result = new DoctorPointCountResult();
        result.setTotalPoint(service.count(new QueryWrapper<DoctorPoint>().lambda()
                .eq(DoctorPoint::getDoctorUserId, SecurityUtils.getUser().getId())
        ));
        result.setWithdraw(service.count(new QueryWrapper<DoctorPoint>().lambda()
                .eq(DoctorPoint::getDoctorUserId, SecurityUtils.getUser().getId())
                .eq(DoctorPoint::getWithdrawStatus, 0)));
        result.setPendingWithdraw(service.count(new QueryWrapper<DoctorPoint>().lambda()
                .eq(DoctorPoint::getDoctorUserId, SecurityUtils.getUser().getId())
                .eq(DoctorPoint::getWithdrawStatus, 1)));
        return RestResponse.ok(result);
    }

    /**
     * 判断医生和团队是否开通图文咨询申请
     */
    @PostMapping("/checkOpen")
    public RestResponse checkOpen(@RequestBody PatientOtherOrder patientOtherOrder) {

        if (patientOtherOrder.getDoctorId() != null) {
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            if (one != null) {
                return RestResponse.ok(one);
            }
        } else {
            //查询团队图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
            if (one != null) {
                return RestResponse.ok(one);
            }
        }
        return RestResponse.ok();
    }

    /**
     * 患者图文咨询申请
     *
     * @return
     */
    @PostMapping("/addPatientOtherOrder")
    public RestResponse addPatientOtherOrder(@RequestBody PatientOtherOrder patientOtherOrder) {
        patientOtherOrder.setUserId(SecurityUtils.getUser().getId());
        patientOtherOrder.setCreateTime(LocalDateTime.now());
        patientOtherOrder.setStatus(1);
        patientOtherOrder.setType(1);
        patientOtherOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        if (patientOtherOrder.getDoctorId() != null) {
            User doctorUser = userService.getById(patientOtherOrder.getDoctorId());
            patientOtherOrder.setDeptId(doctorUser.getDeptId());
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
            //查询团队图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        }
        patientOtherOrderService.save(patientOtherOrder);
        RestResponse restResponse = wxPayFarosService.unifiedOtherOrder(patientOtherOrder.getOrderNo());
        return restResponse;
    }

    @PostMapping("/addPatientOtherOrder1")
    public RestResponse addPatientOtherOrder1(@RequestBody PatientOtherOrder patientOtherOrder) {
        patientOtherOrder.setUserId(SecurityUtils.getUser().getId());
        patientOtherOrder.setCreateTime(LocalDateTime.now());
        patientOtherOrder.setStatus(1);
        patientOtherOrder.setType(1);
        patientOtherOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        if (patientOtherOrder.getDoctorId() != null) {
            User doctorUser = userService.getById(patientOtherOrder.getDoctorId());
            patientOtherOrder.setDeptId(doctorUser.getDeptId());
            //查询医生图文咨询申请价格
            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
                    .eq(DoctorUserAction::getUserId, patientOtherOrder.getDoctorId()));
            patientOtherOrder.setAmount(one.getPrice());
            patientOtherOrder.setHour(one.getHour());
        } else {
            DoctorTeam doctorTeam = doctorTeamService.getById(patientOtherOrder.getDoctorTeamId());
            patientOtherOrder.setDeptId(doctorTeam.getDeptId());
//            //查询团队图文咨询申请价格
//            DoctorUserAction one = doctorUserActionService.getOne(new QueryWrapper<DoctorUserAction>().lambda()
//                    .eq(DoctorUserAction::getTeamId, patientOtherOrder.getDoctorTeamId()));
//            patientOtherOrder.setAmount(one.getPrice());
//            patientOtherOrder.setHour(one.getHour());
        }
        patientOtherOrderService.save(patientOtherOrder);
        PayResultData data = new PayResultData();
        data.setOrderId(patientOtherOrder.getId());
        return RestResponse.ok(data);
    }

    /**
     * 查询图文咨询详情
     *
     * @return
     */
    @GetMapping("/getByPatientOtherOrderId")
    public RestResponse getByPatientOtherOrderId(@RequestParam("id") Integer id) {
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getById(id);
        String imageUrl = patientOtherOrder.getImageUrl();
        if (!StringUtils.isEmpty(imageUrl)) {
            String[] split = imageUrl.split(",");
            List<String> strings = Arrays.asList(split);
            patientOtherOrder.setImageUrlList(strings);

        } else {
            patientOtherOrder.setImageUrlList(new ArrayList<>());
        }
        return RestResponse.ok(patientOtherOrder);
    }


    @Override
    protected Class<DoctorPoint> getEntityClass() {
        return DoctorPoint.class;
    }
}
