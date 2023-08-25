package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.ListByDeviceUserParam;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备用户数量
 */
@Slf4j
@RestController
@RequestMapping("/productStockUserCount")
public class ProductStockUserCountController extends AbstractBaseController<ProductStockUserCountService, ProductStockUserCount> {
    @Resource
    private ProductStockUserMacAddCountService productStockUserMacAddCountService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private PlanUserTrainRecordService planUserTrainRecordService;
    @Resource
    private PneumaticRecordService pneumaticRecordService;
    @Resource
    private PlanUserService planUserService;

    @PostMapping("/update")
    public RestResponse save(@RequestBody ProductStockUserCount productStockUserCount) {
        productStockUserCount.setId(1);
        service.updateById(productStockUserCount);
        String balanceMacAdd = productStockUserCount.getBalanceMacAdd();
        if (!StringUtils.isEmpty(balanceMacAdd)) {
            ProductStockUserMacAddCount productStockUserMacAddCount = productStockUserMacAddCountService.getOne(new QueryWrapper<ProductStockUserMacAddCount>().lambda()
                    .eq(ProductStockUserMacAddCount::getMacAdd, balanceMacAdd));
            if (productStockUserMacAddCount == null) {
                productStockUserMacAddCount = new ProductStockUserMacAddCount();
            }
            productStockUserMacAddCount.setCount(productStockUserCount.getBalanceTrainCount());

            productStockUserMacAddCount.setMacAdd(balanceMacAdd);
            productStockUserMacAddCountService.saveOrUpdate(productStockUserMacAddCount);
        }
        String airTrainMacAdd = productStockUserCount.getAirTrainMacAdd();
        if (!StringUtils.isEmpty(airTrainMacAdd)) {
            ProductStockUserMacAddCount productStockUserMacAddCount = productStockUserMacAddCountService.getOne(new QueryWrapper<ProductStockUserMacAddCount>().lambda()
                    .eq(ProductStockUserMacAddCount::getMacAdd, airTrainMacAdd));
            if (productStockUserMacAddCount == null) {
                productStockUserMacAddCount = new ProductStockUserMacAddCount();
            }
            productStockUserMacAddCount.setCount(productStockUserCount.getAirTrainCount());

            productStockUserMacAddCount.setMacAdd(airTrainMacAdd);
            productStockUserMacAddCountService.saveOrUpdate(productStockUserMacAddCount);
        }
        return RestResponse.ok();
    }

    @GetMapping("/get")
    public RestResponse get(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                            @RequestParam("balanceMacAdd") String balanceMacAdd) {
        List<String> macAdd = new ArrayList<>();
        macAdd.add(airTrainMacAdd);
        macAdd.add(balanceMacAdd);
        List<ProductStockUserMacAddCount> productStockUserMacAddCounts = productStockUserMacAddCountService.list(new QueryWrapper<ProductStockUserMacAddCount>().lambda()
                .in(ProductStockUserMacAddCount::getMacAdd, macAdd));
        Map<String, ProductStockUserMacAddCount> productStockUserMacAddCountHashMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(productStockUserMacAddCounts)) {
            productStockUserMacAddCountHashMap = productStockUserMacAddCounts.stream()
                    .collect(Collectors.toMap(ProductStockUserMacAddCount::getMacAdd, t -> t));
        }
        ProductStockUserCount byId = service.getById(1);
        if (byId != null) {
            ProductStockUserMacAddCount productStockUserMacAddCount = productStockUserMacAddCountHashMap.get(airTrainMacAdd);
            if (productStockUserMacAddCount != null) {
                byId.setAirTrainCount(productStockUserMacAddCount.getCount());
            }
            ProductStockUserMacAddCount productStockUserMacAddCount1 = productStockUserMacAddCountHashMap.get(balanceMacAdd);
            if (productStockUserMacAddCount1 != null) {
                byId.setBalanceTrainCount(productStockUserMacAddCount1.getCount());
            }
        }
        return RestResponse.ok(byId);
    }

    //查询设备的就诊人列表
    @GetMapping("/getPatientUser")
    public RestResponse getPatientUser(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                                       @RequestParam("balanceMacAdd") String balanceMacAdd) {
        List<String> macAdds = new ArrayList<>();
        macAdds.add(airTrainMacAdd);
        macAdds.add(balanceMacAdd);
        List<ProductStock> productStocks = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                .in(ProductStock::getMacAddress, macAdds)
                .eq(ProductStock::getDel, 1));
        if (CollectionUtils.isEmpty(productStocks)) {
            return RestResponse.ok();
        }

        Map<String, List<ProductStock>> productStockMap = productStocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getMacAddress));
        Map<String, List<ProductStock>> productStockMap1 = productStocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getServicePackId));
        List<ProductStock> productStocks1 = productStockMap.get(airTrainMacAdd);
        List<String> servicePackIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(productStocks1)) {
            String servicePackId = productStocks1.get(0).getServicePackId();
            if (!StringUtils.isEmpty(servicePackId)) {
                servicePackIds.add(servicePackId);
            }
        }
        List<ProductStock> productStocks2 = productStockMap.get(balanceMacAdd);
        if (!CollectionUtils.isEmpty(productStocks2)) {
            String servicePackId = productStocks2.get(0).getServicePackId();
            if (!StringUtils.isEmpty(servicePackId)) {
                servicePackIds.add(servicePackId);
            }
        }
        if (CollectionUtils.isEmpty(servicePackIds)) {
            return RestResponse.ok();
        }
        List<UserOrder> userOrders = userOrdertService.list(new QueryWrapper<UserOrder>().lambda()
                .in(UserOrder::getServicePackId, servicePackIds)
                .orderByDesc(UserOrder::getCreateTime).last(" limit 10 "));
        if (CollectionUtils.isEmpty(userOrders)) {
            return RestResponse.ok();
        }
        List<Integer> patientUserIds = userOrders.stream().map(UserOrder::getPatientUserId)
                .collect(Collectors.toList());
        Map<Integer, List<UserOrder>> orderMap = userOrders.stream()
                .collect(Collectors.groupingBy(UserOrder::getPatientUserId));

        List<PatientUser> patientUsers = (List<PatientUser>) patientUserService.listByIds(patientUserIds);
        for (PatientUser patientUser : patientUsers) {
            List<UserOrder> userOrders1 = orderMap.get(Integer.parseInt(patientUser.getId()));
            if (!CollectionUtils.isEmpty(userOrders1)) {
                patientUser.setAddress(userOrders1.get(0).getReceiverDetailAddress());
            }

            if (!org.apache.commons.lang3.StringUtils.isEmpty(patientUser.getIdCard()) && org.apache.commons.lang3.StringUtils.isEmpty(patientUser.getAge())) {
                Map<String, String> map = getAge(patientUser.getIdCard());
                patientUser.setAge(map.get("birthday"));
            }
            if (!org.apache.commons.lang3.StringUtils.isEmpty(patientUser.getIdCard()) && org.apache.commons.lang3.StringUtils.isEmpty(patientUser.getSex())) {
                Map<String, String> map = getAge(patientUser.getIdCard());

                patientUser.setSex(map.get("sexCode"));//1-男0-女
            }

        }
        Map<String, PatientUser> patientUserMap = patientUsers.stream()
                .collect(Collectors.toMap(PatientUser::getId, t -> t));
        List<PatientUser> patientUserList = new ArrayList<>();
        for (UserOrder userOrder : userOrders) {
            PatientUser patientUser = patientUserMap.get(userOrder.getPatientUserId() + "");
            PatientUser patientUser1=new PatientUser();
            BeanUtils.copyProperties(patientUser, patientUser1);
            Integer servicePackId = userOrder.getServicePackId();
            List<ProductStock> productStocks3 = productStockMap1.get(servicePackId + "");
            if (!CollectionUtils.isEmpty(productStocks3)) {
                String macAddress = productStocks3.get(0).getMacAddress();
                patientUser1.setMacAdd(macAddress);
            }
            patientUserList.add(patientUser1);
        }
        return RestResponse.ok(patientUserList);


    }

    //查询设备用户的训练记录
    @GetMapping("/getTrainRecord")
    public RestResponse getTrainRecord(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                                       @RequestParam("balanceMacAdd") String balanceMacAdd) {
        GetTrainRecordData getTrainRecordData = new GetTrainRecordData();
        //气动
        List<PneumaticRecord> pneumaticRecords = pneumaticRecordService.list(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getMacAdd, airTrainMacAdd)
                .orderByDesc(PneumaticRecord::getUpdateTime)
                .orderByDesc(PneumaticRecord::getPlanDayTime).last(" limit 5 "));
        if (!CollectionUtils.isEmpty(pneumaticRecords)) {
            List<String> userIds = pneumaticRecords.stream().map(PneumaticRecord::getUserId)
                    .collect(Collectors.toList());
            String url = "https://api.redadzukibeans.com/system/deviceUser/ListByDeviceUserId";
            ListByDeviceUserParam param = new ListByDeviceUserParam();
            param.setDeviceUserIds(userIds);
            String params = JSONObject.toJSONString(param);
            String post = HttpUtil.post(url, params);
            RestResponse restResponse = JSONObject.parseObject(post, RestResponse.class);
            String data = restResponse.getData().toString();
            List<TbTrainUser> tbTrainUsers = JSONObject.parseArray(data, TbTrainUser.class);

            Map<String, TbTrainUser> userMap = new HashMap<>();
            for (TbTrainUser tbTrainUser : tbTrainUsers) {
                userMap.put(tbTrainUser.getUserId(), tbTrainUser);

            }
            for (PneumaticRecord tbUserTrainRecord : pneumaticRecords) {
                tbUserTrainRecord.setUserName(userMap.get(tbUserTrainRecord.getUserId()).getName());
            }
            Collections.sort(pneumaticRecords);
            getTrainRecordData.setPneumaticRecordList(pneumaticRecords);
        }
        //下肢
        List<TbUserTrainRecord> tbUserTrainRecords = planUserTrainRecordService.list(new QueryWrapper<TbUserTrainRecord>().lambda()
                .eq(TbUserTrainRecord::getMacAddress, balanceMacAdd)
                .orderByDesc(TbUserTrainRecord::getUpdateTime)
                .orderByDesc(TbUserTrainRecord::getDateStr).last(" limit 5 "));
        if (!CollectionUtils.isEmpty(tbUserTrainRecords)) {
            List<String> userIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getUserId)
                    .collect(Collectors.toList());
            List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
                    .in(TbTrainUser::getUserId, userIds));
            Map<String, TbTrainUser> userMap = tbTrainUsers.stream()
                    .collect(Collectors.toMap(TbTrainUser::getUserId, t -> t));
            for (TbUserTrainRecord tbUserTrainRecord : tbUserTrainRecords) {
                tbUserTrainRecord.setUserName(userMap.get(tbUserTrainRecord.getUserId()).getName());
            }
            Collections.sort(tbUserTrainRecords);
            getTrainRecordData.setTbUserTrainRecords(tbUserTrainRecords);
        }
        return RestResponse.ok(getTrainRecordData);


    }

    //根据用户id查询设备用户的训练记录不分页
    @GetMapping("/getTrainRecordByUserId")
    public RestResponse getTrainRecordByUserId(@RequestParam("userId") String userId) {
        GetTrainRecordData getTrainRecordData = new GetTrainRecordData();
        //气动
        List<PneumaticRecord> pneumaticRecords = pneumaticRecordService.list(new QueryWrapper<PneumaticRecord>().lambda()
                .eq(PneumaticRecord::getUserId, userId)
                .orderByDesc(PneumaticRecord::getUpdateTime)
                .orderByDesc(PneumaticRecord::getPlanDayTime));
        if (!CollectionUtils.isEmpty(pneumaticRecords)) {
            List<String> userIds = pneumaticRecords.stream().map(PneumaticRecord::getUserId)
                    .collect(Collectors.toList());
            String url = "https://api.redadzukibeans.com/system/deviceUser/ListByDeviceUserId";
            ListByDeviceUserParam param = new ListByDeviceUserParam();
            param.setDeviceUserIds(userIds);
            String params = JSONObject.toJSONString(param);
            String post = HttpUtil.post(url, params);
            RestResponse restResponse = JSONObject.parseObject(post, RestResponse.class);
            String data = restResponse.getData().toString();
            List<TbTrainUser> tbTrainUsers = JSONObject.parseArray(data, TbTrainUser.class);

            Map<String, TbTrainUser> userMap = new HashMap<>();
            for (TbTrainUser tbTrainUser : tbTrainUsers) {
                userMap.put(tbTrainUser.getUserId(), tbTrainUser);

            }
            for (PneumaticRecord tbUserTrainRecord : pneumaticRecords) {
                tbUserTrainRecord.setUserName(userMap.get(tbUserTrainRecord.getUserId()).getName());
            }
            Collections.sort(pneumaticRecords);
            getTrainRecordData.setPneumaticRecordList(pneumaticRecords);
        }
        //下肢
        List<TbUserTrainRecord> tbUserTrainRecords = planUserTrainRecordService.list(new QueryWrapper<TbUserTrainRecord>().lambda()
                .eq(TbUserTrainRecord::getUserId, userId)
                .orderByDesc(TbUserTrainRecord::getUpdateTime)
                .orderByDesc(TbUserTrainRecord::getDateStr));
        if (!CollectionUtils.isEmpty(tbUserTrainRecords)) {
            List<String> userIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getUserId)
                    .collect(Collectors.toList());
            List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
                    .in(TbTrainUser::getUserId, userIds));
            Map<String, TbTrainUser> userMap = tbTrainUsers.stream()
                    .collect(Collectors.toMap(TbTrainUser::getUserId, t -> t));
            for (TbUserTrainRecord tbUserTrainRecord : tbUserTrainRecords) {
                tbUserTrainRecord.setUserName(userMap.get(tbUserTrainRecord.getUserId()).getName());
            }
            Collections.sort(tbUserTrainRecords);
            getTrainRecordData.setTbUserTrainRecords(tbUserTrainRecords);
        }
        return RestResponse.ok(getTrainRecordData);


    }

    private static Map<String, String> getAge(String idCard) {
        String birthday = "";
        String age = "";
        Integer sexCode = 0;

        int year = Calendar.getInstance().get(Calendar.YEAR);
        char[] number = idCard.toCharArray();
        boolean flag = true;

        if (number.length == 15) {
            for (int x = 0; x < number.length; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        } else if (number.length == 18) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag) {
                    return new HashMap<String, String>();
                }
                flag = Character.isDigit(number[x]);
            }
        }

        if (flag && idCard.length() == 15) {
            birthday = "19" + idCard.substring(6, 8) + "-"
                    + idCard.substring(8, 10) + "-"
                    + idCard.substring(10, 12);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 3, idCard.length())) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt("19" + idCard.substring(6, 8))) + "";
        } else if (flag && idCard.length() == 18) {
            birthday = idCard.substring(6, 10) + "-"
                    + idCard.substring(10, 12) + "-"
                    + idCard.substring(12, 14);
            sexCode = Integer.parseInt(idCard.substring(idCard.length() - 4, idCard.length() - 1)) % 2 == 0 ? 0 : 1;
            age = (year - Integer.parseInt(idCard.substring(6, 10))) + "";
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("birthday", birthday);
        map.put("age", age);
        map.put("sexCode", sexCode + "");
        return map;
    }

    @Override
    protected Class<ProductStockUserCount> getEntityClass() {
        return ProductStockUserCount.class;
    }
}
