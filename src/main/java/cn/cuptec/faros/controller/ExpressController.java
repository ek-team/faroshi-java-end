package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Express;
import cn.cuptec.faros.service.ExpressService;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 快递
 */
@RestController
@RequestMapping("/express")
public class ExpressController extends AbstractBaseController<ExpressService, Express> {

    @GetMapping("/manage/page")
    public RestResponse pageList(){
        Page<Express> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage pageResult = service.page(page, queryWrapper);
        return RestResponse.ok(pageResult);
    }

    @PostMapping("/manage")
    public RestResponse save(@RequestBody Express express){
        service.save(express);
        return RestResponse.ok();
    }

    @DeleteMapping("/manage/{expressCode}")
    public RestResponse delete(@PathVariable String expressCode){
        service.removeById(expressCode);
        return RestResponse.ok();
    }

    @PutMapping("/manage")
    public RestResponse update(@Valid @RequestBody Express express){
        service.updateById(express);
        return RestResponse.ok();
    }

    @GetMapping
    public RestResponse listAll(){
        return RestResponse.ok(service.list());
    }
    /**
     * 获取用户订单物流轨迹信息
     * @param id 订单id
     * @return
     */

    @GetMapping("/user/orderMapTrace")
    public RestResponse getMyExpressInformation(@RequestParam int id){
        MapExpressTrackVo vo = service.getUserOrderMapTrace(id);
        return RestResponse.ok(vo);
    }
    //获取用户回收单物流信息
    @GetMapping("/user/retrieveExpressData")
    public RestResponse getRetrieveOrderExpressData(@RequestParam int id){
        return RestResponse.ok(service.queryRetrieveOrderExpressInfo(id));
    }

    @Override
    protected Class<Express> getEntityClass() {
        return Express.class;
    }
}
