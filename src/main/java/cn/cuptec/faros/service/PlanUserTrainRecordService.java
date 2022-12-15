package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.PlanUserTrainRecordMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanUserTrainRecordService extends ServiceImpl<PlanUserTrainRecordMapper, TbUserTrainRecord> {
    @Resource
    private TrainDataService trainDataService;

    @Resource
    private PlanUserService planUserService;
    @Resource
    private ProductStockService productStockService;

    @Transactional(rollbackFor = Exception.class)
    public void saveAndData(List<TbUserTrainRecord> userTrainRecordList) {
        if (CollUtil.isEmpty(userTrainRecordList)) return;
        this.saveBatch(userTrainRecordList);
        String userId = userTrainRecordList.get(0).getUserId();
        planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()//修改训练记录上传标识
                .eq(TbTrainUser::getUserId, userId)
                .set(TbTrainUser::getTrainRecordTag, 0)
        );
        //更改使用设备序列号
        List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().
                lambda().eq(ProductStock::getMacAddress, userTrainRecordList.get(0).getUserId()).eq(ProductStock::getDel, 1));
        if (!com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(productStocks)) {
            planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()
                    .eq(TbTrainUser::getUserId, userTrainRecordList.get(0).getUserId())
                    .set(TbTrainUser::getUseProductSn, productStocks.get(0).getProductSn())
            );
        }
        for (TbUserTrainRecord record : userTrainRecordList) {
            if (CollUtil.isNotEmpty(record.getTrainDataList())) {
                record.getTrainDataList().forEach(data -> data.setRecordId(record.getId()));
                trainDataService.saveBatch(record.getTrainDataList());
            }

        }


    }

    public IPage<TbUserTrainRecord> pageByUid(Page<TbUserTrainRecord> page, String uid) {


        IPage<TbUserTrainRecord> p = this.pageTrainRecordByUid(page, uid);
        if (CollUtil.isNotEmpty(p.getRecords())) {
            List<Integer> ids = p.getRecords().stream().map(TbUserTrainRecord::getId).collect(Collectors.toList());
            LambdaQueryWrapper wrapper = new QueryWrapper<TbTrainData>().lambda()
                    .in(TbTrainData::getRecordId, ids)
                    .orderByAsc(TbTrainData::getId);
            List<TbTrainData> tbTrainDataList = trainDataService.list(wrapper);
            if (CollUtil.isNotEmpty(tbTrainDataList)) {
                for (TbUserTrainRecord record : p.getRecords()) {
                    List<TbTrainData> collect = tbTrainDataList.stream().filter(sub -> record.getId().equals(sub.getRecordId()))
                            .sorted(Comparator.comparing(TbTrainData::getCreateDate)
                            ).collect(Collectors.toList());
                    record.setTrainDataList(collect);
                }
            }
        }
        return p;
    }


    public IPage<TbUserTrainRecord> pageTrainRecordByUid(Page<TbUserTrainRecord> page, String uid) {

        LambdaQueryWrapper wrapper = new QueryWrapper<TbUserTrainRecord>().lambda()
                .eq(TbUserTrainRecord::getUserId, uid)
                .orderByAsc(TbUserTrainRecord::getCreateDate, TbUserTrainRecord::getFrequency);

        return this.page(page, wrapper);
    }

    public List<TbUserTrainRecord> listTrainRecordByUid(String uid) {

        LambdaQueryWrapper wrapper = new QueryWrapper<TbUserTrainRecord>().lambda()
                .eq(TbUserTrainRecord::getUserId, uid)
                .orderByAsc(TbUserTrainRecord::getCreateDate, TbUserTrainRecord::getFrequency);

        return this.list(wrapper);
    }

    public List<TbUserTrainRecord> trainRecordByUid(String uid) {

        LambdaQueryWrapper wrapper = new QueryWrapper<TbUserTrainRecord>().lambda()
                .eq(TbUserTrainRecord::getUserId, uid)
                .orderByAsc(TbUserTrainRecord::getCreateDate, TbUserTrainRecord::getFrequency);
        List<TbUserTrainRecord> list = this.list(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> ids = list.stream().map(TbUserTrainRecord::getId).collect(Collectors.toList());

            List<TbTrainData> tbTrainDatas = trainDataService.listByRecordIds(ids);
            if (!CollectionUtils.isEmpty(tbTrainDatas)) {
                Map<Integer, List<TbTrainData>> map = tbTrainDatas.stream()
                        .collect(Collectors.groupingBy(TbTrainData::getRecordId));
                for (TbUserTrainRecord tbUserTrainRecord : list) {
                    tbUserTrainRecord.setTrainDataList(map.get(tbUserTrainRecord.getId()));
                }
            }

        }
        return list;
    }

    public IPage<TbUserTrainRecord> pageTrainRecordByXtUserId(Page<TbUserTrainRecord> page, Integer xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, xtUserId));//getInfoByUXtUserId(xtUserId);
        if (infoByUXtUserId == null) return null;

        return pageTrainRecordByUid(page, infoByUXtUserId.getUserId());
    }

    public IPage<TbUserTrainRecord> pageTrainRecordById(Page<TbUserTrainRecord> page, Integer xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getId, xtUserId));//getInfoByUXtUserId(xtUserId);
        if (infoByUXtUserId == null) return page;

        return pageTrainRecordByUid(page, infoByUXtUserId.getUserId());
    }

    public List<TbUserTrainRecord> listTrainRecordByXtUserId(Integer xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getId, xtUserId));//getInfoByUXtUserId(xtUserId);
        if (infoByUXtUserId == null) return new ArrayList<>();

        return listTrainRecordByUid(infoByUXtUserId.getUserId());
    }

    public List<TbUserTrainRecord> trainRecordByPhone(String phone, String idCard,String xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getInfoByPhoneAndIdCard(phone, idCard,xtUserId);
        if (infoByUXtUserId == null) return new ArrayList<>();

        return trainRecordByUid(infoByUXtUserId.getUserId());
    }

}
