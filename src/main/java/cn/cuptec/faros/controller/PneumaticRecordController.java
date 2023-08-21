package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/pneumaticRecord")
@Slf4j
public class PneumaticRecordController extends AbstractBaseController<PneumaticRecordService, PneumaticRecord> {
    private final Url url;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private PneumaticPlanService pneumaticPlanService;
    @Resource
    private TrainNumberMacAddService trainNumberMacAddService;
    @Resource
    private ProductStockService productStockService;

    //添加记录
    @PostMapping("/save")
    public RestResponse save(@RequestBody List<PneumaticRecord> pneumaticRecords) {


        if (!CollectionUtils.isEmpty(pneumaticRecords)) {
            //计划的训练记录日期「planDayTime」与上传的训练记录日期「planDayTime」一致，并且keyId相同就覆盖更新，不存在就插入


            for (PneumaticRecord pneumaticRecord : pneumaticRecords) {
                pneumaticRecord.setPlanDayTime(pneumaticRecord.getPlanDayTime() + " 00:00:00");
                service.remove(new QueryWrapper<PneumaticRecord>().
                        lambda().eq(PneumaticRecord::getUserId, pneumaticRecords.get(0).getUserId())
                        .eq(PneumaticRecord::getPlanDayTime, pneumaticRecord.getPlanDayTime())
                        .eq(PneumaticRecord::getKeyId, pneumaticRecord.getKeyId()));
            }


            service.saveBatch(pneumaticRecords);
            pushUseCount(pneumaticRecords.get(0).getMacAddress());
        }
        return RestResponse.ok();
    }

    //发送设备使用次数加1
    private void pushUseCount(String macAddress) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                TrainNumberMacAdd trainNumberMacAdd = trainNumberMacAddService.getOne(new QueryWrapper<TrainNumberMacAdd>().lambda()
                        .eq(TrainNumberMacAdd::getMacAdd, macAddress));
                if (trainNumberMacAdd == null) {
                    trainNumberMacAdd = new TrainNumberMacAdd();
                    trainNumberMacAdd.setTrainNumber(1);
                } else {
                    trainNumberMacAdd.setTrainNumber(trainNumberMacAdd.getTrainNumber() + 1);
                }
                trainNumberMacAdd.setMacAdd(macAddress);
                trainNumberMacAddService.saveOrUpdate(trainNumberMacAdd);
                Channel targetUserChannel = UserChannelManager.getUserChannelByMacAdd(macAddress);
                //2.向目标用户发送新消息提醒n
                if (targetUserChannel != null) {

                    SocketFrameTextMessage targetUserMessage
                            = SocketFrameTextMessage.PRODUCT_STOCK_TRAIN_RECORD_COUNT(trainNumberMacAdd.getTrainNumber());

                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));

                }


            }
        });
    }

    /**
     * 根据用户id查询气动记录数据
     *
     * @return
     */
    @GetMapping("/getByUserId")
    public RestResponse getByUserId(@RequestParam("userId") String userId) {
        List<PneumaticRecord> list = service.list(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getUserId, userId));
        Map<String, List<PneumaticRecord>> labelMap = list.stream()
                .collect(Collectors.groupingBy(PneumaticRecord::getPlanDayTime));
        //根据时间排序
        Map<String, List<PneumaticRecord>> map = new TreeMap<String, List<PneumaticRecord>>(
                new Comparator<String>() {
                    @Override
                    public int compare(String obj1, String obj2) {
                        // 升序排序
                        return obj1.compareTo(obj2);
                    }
                });


        for (String key : labelMap.keySet()) {
            map.put(key, labelMap.get(key));
        }
        return RestResponse.ok(map);
    }

    /**
     * 根据用户id查询气动记录数据 给设备调用
     *
     * @return
     */
    @GetMapping("/getByUserIdData")
    public RestResponse getByUserIdData(@RequestParam("userId") String userId, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        List<PneumaticRecord> list = new ArrayList<>();
        if (StringUtils.isEmpty(startDate)) {
            list = service.list(new QueryWrapper<PneumaticRecord>().lambda()
                    .eq(PneumaticRecord::getUserId, userId));
        } else {
            startDate = startDate + " 00:00:00";
            endDate = endDate + " 00:00:00";
            list = service.list(new QueryWrapper<PneumaticRecord>().lambda()
                    .eq(PneumaticRecord::getUserId, userId).ge(PneumaticRecord::getPlanDayTime, startDate)
                    .le(PneumaticRecord::getPlanDayTime, endDate));
        }

        Map<String, List<PneumaticRecord>> labelMap = list.stream()
                .collect(Collectors.groupingBy(PneumaticRecord::getPlanDayTime));
        //根据时间排序
//        Map<String, List<PneumaticRecord>> map = new TreeMap<String, List<PneumaticRecord>>(
////                new Comparator<String>() {
////                    @Override
////                    public int compare(String obj1, String obj2) {
////                        // 升序排序
////                        return obj1.compareTo(obj2);
////                    }
////                });
        List<PneumaticRecordResult> recordResults = new ArrayList<>();
        for (String key : labelMap.keySet()) {
            PneumaticRecordResult recordResult = new PneumaticRecordResult();
            recordResult.setPlanDayTime(key);
            recordResult.setRecords(labelMap.get(key));
            recordResults.add(recordResult);
        }
        return RestResponse.ok(recordResults);
    }

    @GetMapping("/getDataByPlanDayTime")
    public RestResponse getDataByPlanDayTime(@RequestParam("userId") String userId, @RequestParam("planDayTime") String planDayTime) {
        planDayTime = planDayTime + " 00:00:00";

        return RestResponse.ok(service.list(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getUserId, userId)
                .eq(PneumaticRecord::getPlanDayTime, planDayTime)));
    }

    /**
     * 根据时间清除记录planDayTime
     *
     * @return
     */
    @GetMapping("/clearRecordByPlanDayTime")
    public RestResponse clearRecordByPlanDayTime(@RequestParam("userId") String userId, @RequestParam("planDayTime") String planDayTime) {
        planDayTime = planDayTime + " 00:00:00";
        return RestResponse.ok(service.remove(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getUserId, userId)
                .eq(PneumaticRecord::getPlanDayTime, planDayTime)));
    }

    /**
     * 根据userId删除
     *
     * @return
     */
    @GetMapping("/clearRecordByPlanUserId")
    public RestResponse clearRecordByPlanDayTime(@RequestParam("userId") String userId) {
        return RestResponse.ok(service.remove(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getUserId, userId)
        ));
    }

    /**
     * 解析气动训练数据页面 二维码跳转
     *
     * @return
     */
    @SneakyThrows
    @GetMapping("/pneumaticRecordPage/{idCard}")
    public void registerPlanUser(@PathVariable String idCard, HttpServletResponse response) {
        List<TbTrainUser> list = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, idCard));
        if (!CollectionUtils.isEmpty(list)) {
            TbTrainUser tbTrainUser = list.get(0);
            response.sendRedirect(url.getUrl() + QrCodeConstants.PNEUMATIC_Record_PAGE_URL + "?userId=" + tbTrainUser.getUserId());
        }

    }

    @Override
    protected Class<PneumaticRecord> getEntityClass() {
        return PneumaticRecord.class;
    }
}
