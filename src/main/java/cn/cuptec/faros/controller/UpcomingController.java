package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.entity.Upcoming;
import cn.cuptec.faros.service.UpcomingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 待办事项
 */
@RestController
@RequestMapping("/upcoming")
public class UpcomingController extends AbstractBaseController<UpcomingService, Upcoming> {


    /**
     * 分页查询待办事项
     */
    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Integer uid = SecurityUtils.getUser().getId();
        Page<Upcoming> page = getPage();
        IPage<Upcoming> page1 = service.page(page, new QueryWrapper<Upcoming>().lambda().eq(Upcoming::getUserId, uid).orderByDesc(Upcoming::getCreateTime, Upcoming::getRedStatus));
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
        Upcoming upcoming=new Upcoming();
        upcoming.setId(id);
        upcoming.setRedStatus(1);
        service.updateById(upcoming);
        return RestResponse.ok(service.getById(id));
    }
    /**
     * 一键设置全部已读待处理业务
     * @return
     */
    @GetMapping("/updateAllRead")
    public RestResponse updateAllRead() {

        service.update(Wrappers.<Upcoming>lambdaUpdate()
                .set(Upcoming::getRedStatus, 1)
                .eq(Upcoming::getUserId, SecurityUtils.getUser().getId())
        );
        return RestResponse.ok();
    }

    @Override
    protected Class<Upcoming> getEntityClass() {
        return Upcoming.class;
    }
}
