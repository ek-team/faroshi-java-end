package cn.cuptec.faros.service;

import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.InvoiceOption;
import cn.cuptec.faros.entity.ProtocolOption;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.mapper.ProtocolOptionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProtocolOptionService extends ServiceImpl<ProtocolOptionMapper, ProtocolOption> {

    @Resource
    private UserService userService;

    public Integer getIsOpenByUserId(Integer id) {

        Integer isOpen = 1;


        User byId = userService.getById(id);
        if(byId != null && byId.getDeptId() != null){
            ProtocolOption protocolOption = this.getById(byId.getDeptId());
            if (protocolOption != null ){
                isOpen  =protocolOption.getIsOpen() ==null?isOpen:protocolOption.getIsOpen();
            }
        }

        return isOpen;
    }

    public boolean saveOrUpdate(ProtocolOption protocolOption) {

        Integer id = SecurityUtils.getUser().getId();
        User byId = userService.getById(id);
        if(byId == null)
            throw  new InnerException("无权限");
        else if (byId.getDeptId() == null)
            throw  new InnerException("请设置所在部门");
        protocolOption.setDeptId(byId.getDeptId());


        return  super.saveOrUpdate(protocolOption);
    }

}