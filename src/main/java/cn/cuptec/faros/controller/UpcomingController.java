package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.UpcomingService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 待办事项
 */
@RestController
@RequestMapping("/upcoming")
public class UpcomingController extends AbstractBaseController<UpcomingService, Upcoming> {

    @Resource
    private UserService userService;

    /**
     * 分页查询待办事项
     */
    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Integer uid = SecurityUtils.getUser().getId();
        Page<Upcoming> page = getPage();
        IPage<Upcoming> page1 = service.page(page, new QueryWrapper<Upcoming>().lambda().eq(Upcoming::getDoctorId, uid).orderByDesc(Upcoming::getCreateTime, Upcoming::getRedStatus));
        List<Upcoming> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Integer> userIds = records.stream().map(Upcoming::getUserId)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            for (Upcoming upcoming : records) {
                upcoming.setUser(userMap.get(upcoming.getUserId()));
            }
            page1.setRecords(records);
        }

        return RestResponse.ok(page1);
    }

    /**
     * 修改待办事项
     */
    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Upcoming upcoming) {
        service.updateById(upcoming);
        return RestResponse.ok();
    }

    /**
     * 查询待办事项详情
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") int id) {
        Upcoming upcoming = new Upcoming();
        upcoming.setId(id);
        upcoming.setRedStatus(1);
        service.updateById(upcoming);
        return RestResponse.ok(service.getById(id));
    }

    /**
     * 一键设置全部已读待处理业务
     *
     * @return
     */
    @GetMapping("/updateAllRead")
    public RestResponse updateAllRead() {

        service.update(Wrappers.<Upcoming>lambdaUpdate()
                .set(Upcoming::getRedStatus, 1)
                .eq(Upcoming::getDoctorId, SecurityUtils.getUser().getId())
        );
        return RestResponse.ok();
    }

    /**
     * 查询待办事项数量
     *
     * @return
     */
    @GetMapping("/getCount")
    public RestResponse getCount() {


        return RestResponse.ok(service.count(Wrappers.<Upcoming>lambdaUpdate()
                .set(Upcoming::getRedStatus, 0)
                .eq(Upcoming::getDoctorId, SecurityUtils.getUser().getId())
        ));
    }


    @Override
    protected Class<Upcoming> getEntityClass() {
        return Upcoming.class;
    }
}
