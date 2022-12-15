package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.PurchaseOrder;
import cn.cuptec.faros.service.DeptService;
import cn.cuptec.faros.service.PurchaseOrdertService;
import cn.cuptec.faros.service.ProductStockService;
import cn.cuptec.faros.service.UserRoleService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购单
 */
@RestController
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends AbstractBaseController<PurchaseOrdertService, PurchaseOrder> {

    @Resource
    private DeptService deptService;

    @Resource
    private UserRoleService userRoleService;

    @GetMapping("/pageMy")
    public RestResponse pageMy(){
        Page<PurchaseOrder> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage pageResult = service.pageMy(page, queryWrapper);
        return RestResponse.ok(pageResult);
    }

    /**
     * 销售员的申请单列表
     * @param
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped(){
        Page<PurchaseOrder> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage<PurchaseOrder> data = service.pageScoped(page, queryWrapper);
        CustomUser user = SecurityUtils.getUser();

        if(CollUtil.isNotEmpty(data.getRecords())){
            data.getRecords().forEach(purchaseOrder -> purchaseOrder.setIsConfirm(0));
            List<Dept> subList = deptService.getMySubListByIsAdmin();
            if( CollUtil.isNotEmpty(subList)){
                List<Integer> collect = subList.stream().map(Dept::getId).collect(Collectors.toList());
                data.getRecords().forEach(purchaseOrder -> {
                    if(collect.contains(purchaseOrder.getDeptId()))
                        purchaseOrder.setIsConfirm(1);
                });
            }

        }

        return RestResponse.ok(data);
    }

    @PostMapping
    public RestResponse<PurchaseOrder> addOrder(@RequestBody PurchaseOrder purchaseOrder) {
        service.save(purchaseOrder);
        return RestResponse.ok(purchaseOrder);
    }

    @PutMapping("/confirm/{id}")
    public RestResponse confirmPurchase(@PathVariable int id){
        service.confirmPurchase(id);
        return RestResponse.ok();
    }

    @PutMapping("/cancel/{id}")
    public RestResponse cancelPurchase(@PathVariable int id){
        service.cancelPurchase(id);
        return RestResponse.ok();
    }

    @PutMapping
    public RestResponse put(@RequestBody PurchaseOrder purchaseOrder){
        service.updateById(purchaseOrder);
        return RestResponse.ok();
    }

    @DeleteMapping("/{id}")
    public RestResponse deleteById(@PathVariable int id){
        service.removeById(id);
        return RestResponse.ok();
    }

    @Override
    protected Class<PurchaseOrder> getEntityClass() {
        return PurchaseOrder.class;
    }
}
