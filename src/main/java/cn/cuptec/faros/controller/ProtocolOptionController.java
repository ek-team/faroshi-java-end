package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.InvoiceOption;
import cn.cuptec.faros.entity.ProtocolOption;
import cn.cuptec.faros.service.ProtocolOptionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/protocolOption")
public class ProtocolOptionController extends AbstractBaseController<ProtocolOptionService, ProtocolOption> {

    @GetMapping("getIsOpenByUserId")
    public RestResponse getIsOpenByUserId(@RequestParam("userId") Integer userId) {


        return RestResponse.ok(service.getIsOpenByUserId(userId));
    }


    @GetMapping("getIsOpenByMyDept")
    public RestResponse getIsOpenByMyDept() {


        return RestResponse.ok(service.getIsOpenByUserId(SecurityUtils.getUser().getId()));
    }



    @PostMapping("/saveOrUpdate")
    @PreAuthorize("@pms.hasPermission('sys_protocol_option_update')")
    public RestResponse saveOrUpdate(@RequestBody ProtocolOption protocolOption) {
        service.saveOrUpdate(protocolOption);

        return RestResponse.ok();
    }

    @Override
    protected Class<ProtocolOption> getEntityClass() {
        return ProtocolOption.class;
    }
}