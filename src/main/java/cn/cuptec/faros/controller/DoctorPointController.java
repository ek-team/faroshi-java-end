package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.DoctorPointCountResult;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorPointService;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.DoctorUserActionService;
import cn.cuptec.faros.service.PatientOtherOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
     * 患者图文咨询申请
     * @return
     */
    @PostMapping("/addPatientOtherOrder")
    public RestResponse addPatientOtherOrder(@RequestBody PatientOtherOrder patientOtherOrder) {
        patientOtherOrder.setUserId(SecurityUtils.getUser().getId());
        patientOtherOrder.setCreateTime(LocalDateTime.now());
        if(patientOtherOrder.getDoctorId()!=null){
            //查询医生图文咨询申请价格

        }else {
            //查询团队图文咨询申请价格
        }
        return RestResponse.ok();
    }

    @Override
    protected Class<DoctorPoint> getEntityClass() {
        return DoctorPoint.class;
    }
}
