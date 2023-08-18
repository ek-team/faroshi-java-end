package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
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
                productStockUserMacAddCount.setCount(productStockUserCount.getBalanceTrainCount());
            } else {
                productStockUserMacAddCount.setCount(productStockUserMacAddCount.getCount() + productStockUserCount.getBalanceTrainCount());
            }
            productStockUserMacAddCountService.saveOrUpdate(productStockUserMacAddCount);
        }
        String airTrainMacAdd = productStockUserCount.getAirTrainMacAdd();
        if (!StringUtils.isEmpty(airTrainMacAdd)) {
            ProductStockUserMacAddCount productStockUserMacAddCount = productStockUserMacAddCountService.getOne(new QueryWrapper<ProductStockUserMacAddCount>().lambda()
                    .eq(ProductStockUserMacAddCount::getMacAdd, airTrainMacAdd));
            if (productStockUserMacAddCount == null) {
                productStockUserMacAddCount = new ProductStockUserMacAddCount();
                productStockUserMacAddCount.setCount(productStockUserCount.getAirTrainCount());
            } else {
                productStockUserMacAddCount.setCount(productStockUserMacAddCount.getCount() + productStockUserCount.getAirTrainCount());
            }
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
                .orderByDesc(UserOrder::getCreateTime).last(" limit 3 "));
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
        return RestResponse.ok();


    }

    //查询设备用户的训练记录
    @GetMapping("/getTrainRecord")
    public RestResponse getTrainRecord(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                                       @RequestParam("balanceMacAdd") String balanceMacAdd) {
        List<String> macAdds = new ArrayList<>();
        macAdds.add(airTrainMacAdd);
        macAdds.add(balanceMacAdd);
        List<TbUserTrainRecord> tbUserTrainRecords = planUserTrainRecordService.list(new QueryWrapper<TbUserTrainRecord>().lambda()
                .in(TbUserTrainRecord::getMacAddress, macAdds)
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
        }
        return RestResponse.ok(tbUserTrainRecords);


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