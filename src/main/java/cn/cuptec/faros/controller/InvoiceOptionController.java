package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.InvoiceOption;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.InvoiceOptionService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/invoiceOption")
public class InvoiceOptionController extends AbstractBaseController<InvoiceOptionService, InvoiceOption> {




    @GetMapping("getIsOpen")
    public RestResponse getIsOpen() {

        Integer id = SecurityUtils.getUser().getId();

        return RestResponse.ok(service.getIsOpenByUserId(id));
    }



    @PostMapping("/saveOrUpdate")
    @PreAuthorize("@pms.hasPermission('sys_invoice_option_update')")
    public RestResponse saveOrUpdate(@RequestBody InvoiceOption invoiceOption) {
        service.saveOrUpdate(invoiceOption);

        return RestResponse.ok();
    }



    @Override
    protected Class<InvoiceOption> getEntityClass() {
        return InvoiceOption.class;
    }
}