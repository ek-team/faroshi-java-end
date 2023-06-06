package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.PlanUserTrainRecordMapper;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PlanUserTrainRecordService extends ServiceImpl<PlanUserTrainRecordMapper, TbUserTrainRecord> {
    @Resource
    private TrainDataService trainDataService;

    @Resource
    private PlanUserService planUserService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private UserOrdertService userOrdertService;
    @Autowired
    public RedisTemplate redisTemplate;


    @Transactional(rollbackFor = Exception.class)
    public void saveAndData(List<TbUserTrainRecord> userTrainRecordList) {
        if (CollUtil.isEmpty(userTrainRecordList)) return;
        this.saveBatch(userTrainRecordList);
        String userId = userTrainRecordList.get(0).getUserId();
        //判断是否是第一次上传训练记录
        List<TbUserTrainRecord> list = list(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getUserId, userId));
        if (CollectionUtils.isEmpty(list)) {

            planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()//修改训练记录上传标识
                    .eq(TbTrainUser::getUserId, userId)
                    .set(TbTrainUser::getTrainRecordTag, 0)
                    .set(TbTrainUser::getFirstTrainTime, LocalDateTime.now())
            );
            TbTrainUser one = planUserService.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));

            List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getUserId, one.getXtUserId()).orderByDesc(UserOrder::getPayTime));
            if (!CollectionUtils.isEmpty(userOrders)) {
                UserOrder userOrder = userOrders.get(0);
                if (userOrder.getOrderType().equals(2)) {
                    for (int i = 1; i < 4; i++) {
                        String keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "3:", userOrder.getId()));
                        LocalDateTime localDateTime = LocalDateTime.now().plusMonths(i);
                        LocalDateTime localDateTime3 = localDateTime.minusDays(3);
                        Duration sjc = Duration.between(LocalDateTime.now(), localDateTime3);// 计算时间差
                        redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc.toDays(), TimeUnit.DAYS);//设置过期时间
                        //5天
                        keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "5:", userOrder.getId()));
                        LocalDateTime localDateTime5 = localDateTime.minusDays(5);
                        Duration sjc5 = Duration.between(LocalDateTime.now(), localDateTime5);// 计算时间差
                        redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc5.toDays(), TimeUnit.DAYS);//设置过期时间
                        //7天
                        keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "7:", userOrder.getId()));
                        LocalDateTime localDateTime7 = localDateTime.minusDays(7);
                        Duration sjc7 = Duration.between(LocalDateTime.now(), localDateTime7);// 计算时间差
                        redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc7.toDays(), TimeUnit.DAYS);//设置过期时间

                    }

                }

            }


        } else {

            planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()//修改训练记录上传标识
                    .eq(TbTrainUser::getUserId, userId)
                    .set(TbTrainUser::getTrainRecordTag, 0)
            );
        }


        for (TbUserTrainRecord record : userTrainRecordList) {
            if (CollUtil.isNotEmpty(record.getTrainDataList())) {
                record.getTrainDataList().forEach(data -> data.setRecordId(record.getId()));
                trainDataService.saveBatch(record.getTrainDataList());
            }

        }

        List<ProductStock> list1 = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                .eq(ProductStock::getProductSn, userTrainRecordList.get(0).getProductSn())
                .eq(ProductStock::getDel, 1));
        if (!CollectionUtils.isEmpty(list1)) {
            //更改使用设备序列号
            planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()
                    .eq(TbTrainUser::getUserId, userTrainRecordList.get(0).getUserId())
                    .set(TbTrainUser::getUseProductSn, userTrainRecordList.get(0).getProductSn() + "/" + list1.get(0).getMacAddress())
            );

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

    public List<TbUserTrainRecord> trainRecordByPhone(String phone, String idCard, String xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getInfoByPhoneAndIdCard(phone, idCard, xtUserId);
        if (infoByUXtUserId == null) return new ArrayList<>();

        return trainRecordByUid(infoByUXtUserId.getUserId());
    }

}
