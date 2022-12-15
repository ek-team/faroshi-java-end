package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ListByUidPlanResult;
import cn.cuptec.faros.entity.OperationRecord;
import cn.cuptec.faros.entity.TbPlan;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.OperationRecordService;
import cn.cuptec.faros.service.UserService;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
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
    public RestResponse listByUid() {
        List<OperationRecord> list = operationRecordService.list();
        if(CollectionUtils.isEmpty(list)){
            return RestResponse.ok();
        }
        List<String> userIds = list.stream().map(OperationRecord::getUserId)
                .collect(Collectors.toList());
        List<User> users = (List<User>) userService.listByIds(userIds);
        Map<Integer, User> accountMap = users.stream()
                .collect(Collectors.toMap(User::getId, t -> t));
        for(OperationRecord operationRecord:list){
            User user = accountMap.get(Integer.parseInt(operationRecord.getUserId()));
            if(user!=null){
                operationRecord.setUserName(user.getNickname());
            }
        }
        return RestResponse.ok(list);
    }

    @Override
    protected Class<OperationRecord> getEntityClass() {
        return OperationRecord.class;
    }
}
