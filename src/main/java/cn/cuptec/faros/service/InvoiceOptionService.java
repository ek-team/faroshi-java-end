package cn.cuptec.faros.service;

import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.InvoiceOption;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.mapper.InvoiceOptionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InvoiceOptionService extends ServiceImpl<InvoiceOptionMapper, InvoiceOption> {

    @Resource
    private UserService userService;

    public Integer getIsOpenByUserId(Integer id) {

        Integer isOpen = 1;


        User byId = userService.getById(id);
        if(byId != null && byId.getDeptId() != null){
            InvoiceOption invoiceOption = this.getById(byId.getDeptId());
            if (invoiceOption != null ){
                isOpen  =invoiceOption.getIsOpen() ==null?isOpen:invoiceOption.getIsOpen();
            }
        }

        return isOpen;
    }

    public boolean saveOrUpdate(InvoiceOption invoiceOption) {

        Integer id = SecurityUtils.getUser().getId();
        User byId = userService.getById(id);
        if(byId == null)
            throw  new InnerException("无权限");
        else if (byId.getDeptId() == null)
            throw  new InnerException("请设置所在部门");
        invoiceOption.setDeptId(byId.getDeptId());


        return  super.saveOrUpdate(invoiceOption);
    }
}