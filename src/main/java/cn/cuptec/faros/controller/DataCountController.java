package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.CustomProduct;
import cn.cuptec.faros.entity.DataCount;
import cn.cuptec.faros.service.CustomProductService;
import cn.cuptec.faros.service.DataCountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dataCount")
public class DataCountController extends AbstractBaseController<DataCountService, DataCount> {
    @PostMapping("update")
    public RestResponse update(@RequestBody DataCount dataCount){
        dataCount.setId(1);
        service.updateById(dataCount);
        return RestResponse.ok();
    }
    @Override
    protected Class<DataCount> getEntityClass() {
        return DataCount.class;
    }
}
