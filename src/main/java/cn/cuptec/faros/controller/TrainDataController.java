package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.TbTrainData;
import cn.cuptec.faros.entity.TbUserTrainRecord;
import cn.cuptec.faros.service.TrainDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainData")
public class TrainDataController extends AbstractBaseController<TrainDataService, TbTrainData> {


    @GetMapping("/listByRecordId")
    public RestResponse listByRecordId(@RequestParam("recordId") Integer recordId){



        return RestResponse.ok(service.listByRecordId( recordId));
    }
//
//    @PostMapping("/save")
//    public RestResponse<TbTrainData> addTrainData(@RequestBody TbTrainData trainDataEntity) {
//        service.save(trainDataEntity);
//        return RestResponse.ok();
//    }
//
//    @GetMapping("pageByUid")
//    public RestResponse pageByUid(@RequestParam(name = "userId") long uid){
//        Page<TbTrainData> page = getPage();
//        IPage<TbTrainData> trainDataIPage = service.page(page, Wrappers.<TbTrainData>lambdaQuery().eq(TbTrainData::getUserId, uid).orderByDesc(TbTrainData::getCreateDate));
//        return RestResponse.ok(trainDataIPage);
//    }
//
//    @GetMapping("listByUid")
//    public RestResponse listByUid(@RequestParam(name = "userId") long uid){
//        List<TbTrainData> list = service.list(Wrappers.<TbTrainData>lambdaQuery().eq(TbTrainData::getUserId, uid).orderByDesc(TbTrainData::getCreateDate));
//        return RestResponse.ok(list);
//    }


    @Override
    protected Class<TbTrainData> getEntityClass() {
        return TbTrainData.class;
    }
}
