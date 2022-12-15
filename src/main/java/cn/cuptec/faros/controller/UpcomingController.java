package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Upcoming;
import cn.cuptec.faros.service.UpcomingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        queryWrapper.select().isNotNull("na_li_order_id");
        Page<Upcoming> page = getPage();
        IPage<Upcoming> page1 = service.page(page, new QueryWrapper<Upcoming>().lambda().eq(Upcoming::getUserId, uid).orderByDesc(Upcoming::getCreateTime, Upcoming::getRedStatus));
        return RestResponse.ok(page1);
    }

    /**
     * 修改待办事项
     */
    @GetMapping("/updateById")
    public RestResponse updateById(@RequestBody Upcoming upcoming) {
        service.updateById(upcoming);
        return RestResponse.ok();
    }

    @Override
    protected Class<Upcoming> getEntityClass() {
        return Upcoming.class;
    }
}
