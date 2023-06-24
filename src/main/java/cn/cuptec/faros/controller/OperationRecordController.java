package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.OperationRecordService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/operationRecord")
public class OperationRecordController extends AbstractBaseController<OperationRecordService, OperationRecord> {
    @Resource
    private OperationRecordService operationRecordService;
    @Resource
    private UserService userService;

    @GetMapping("/list")
    public RestResponse listByUid(@RequestParam(value = "str", required = false) String str,
                                  @RequestParam(value = "type", required = false) Integer type) {
        LambdaQueryWrapper<OperationRecord> eq = new QueryWrapper<OperationRecord>().lambda();
        Page<OperationRecord> page = getPage();
        if (!StringUtils.isEmpty(str)) {
            eq.eq(OperationRecord::getStr, str);
        }
        if (type != null) {
            eq.eq(OperationRecord::getType, type);
        }
        IPage<OperationRecord> page1 = operationRecordService.page(page, eq);
        List<OperationRecord> list = page1.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<String> userIds = list.stream().map(OperationRecord::getUserId)
                .collect(Collectors.toList());
        List<User> users = (List<User>) userService.listByIds(userIds);
        Map<Integer, User> accountMap = users.stream()
                .collect(Collectors.toMap(User::getId, t -> t));
        for (OperationRecord operationRecord : list) {
            if(!StringUtils.isEmpty(operationRecord.getUserId())){
                User user = accountMap.get(Integer.parseInt(operationRecord.getUserId()));
                if (user != null) {
                    operationRecord.setUserName(user.getNickname());
                }
            }

        }
        return RestResponse.ok(page1);
    }

    @PostMapping("/save")
    public RestResponse save(@RequestBody OperationRecord operationRecord) {
        operationRecord.setCreateTime(new Date());
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        service.save(operationRecord);
        return RestResponse.ok();
    }

    @Override
    protected Class<OperationRecord> getEntityClass() {
        return OperationRecord.class;
    }
}
