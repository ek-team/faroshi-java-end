package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.UserDoctorRelation;
import cn.cuptec.faros.service.UserDoctorRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/UserDoctorRelation")
public class UserDoctorRelationController extends AbstractBaseController<UserDoctorRelationService, UserDoctorRelation> {


    @GetMapping("getDoctorByUser")
    public RestResponse getDoctorByUser() {
        return RestResponse.ok(service.getDoctorByUser(SecurityUtils.getUser().getId()));
    }

    @GetMapping("getMyDoctorPage")
    public RestResponse getMyDoctorPage() {
        Page<UserDoctorRelation> page = getPage();


        return RestResponse.ok(service.getMyDoctorPage( page,SecurityUtils.getUser().getId()));
    }

    @GetMapping("getDoctorByUserId")
    public RestResponse getDoctorByUserId(@RequestParam("userId") int userId) {
        return RestResponse.ok(service.getDoctorByUser(userId));
    }


    @PostMapping("/save")
    public RestResponse save(@RequestBody UserDoctorRelation userDoctorRelation) {
        LambdaQueryWrapper<UserDoctorRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDoctorRelation::getDoctorId, userDoctorRelation.getDoctorId());
        queryWrapper.eq(UserDoctorRelation::getUserId, userDoctorRelation.getUserId());
        UserDoctorRelation one = service.getOne(queryWrapper);
        if (one != null) {
            return RestResponse.ok();
        }
        return RestResponse.ok(service.save(userDoctorRelation));
    }


    @Override
    protected Class<UserDoctorRelation> getEntityClass() {
        return UserDoctorRelation.class;
    }
}