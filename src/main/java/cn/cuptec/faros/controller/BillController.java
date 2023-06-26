package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.DeviceLog;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.BillService;
import cn.cuptec.faros.service.UserOrdertService;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 用户填写发票信息
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/bill")
public class BillController {
    @Resource
    private BillService billService;
    @Resource
    private UserOrdertService userOrdertService;
    private static final CloseableHttpClient httpclient = HttpClients.createDefault();
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";


    /**
     * 查询企业信息
     *
     * @return
     */

    @GetMapping("/getCompany")
    public RestResponse getCompany(@RequestParam("name") String name) {
        String url = "https://api.qichacha.com/EnterpriseInfo/Verify?key=96d04ac2d59d46ffbfa79eb6af6e69d5&searchKey=" + name;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", userAgent);
        //SecretKey  E3A1E9ECD707219FAA04C125DF7A791B
        //1027
        String Timespan = System.currentTimeMillis() + "";
        String Token = DigestUtils.md5Hex("96d04ac2d59d46ffbfa79eb6af6e69d5" + Timespan + "E3A1E9ECD707219FAA04C125DF7A791B");

        httpGet.setHeader("Token", Token);
        httpGet.setHeader("Timespan", Timespan);
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                log.info(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return RestResponse.ok();
    }


    @PostMapping("/add")
    public RestResponse save(@RequestBody Bill bill) {
        bill.setUserId(SecurityUtils.getUser().getId());
        bill.setCreateTime(LocalDateTime.now());
        billService.save(bill);
        UserOrder userOrder = new UserOrder();
        userOrder.setOrderNo(bill.getOrderNo());
        userOrder.setBillId(bill.getId());
        String orderNo = bill.getOrderNo();
        String[] split = orderNo.split("KF");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }
        userOrdertService.update(Wrappers.<UserOrder>lambdaUpdate()
                .eq(UserOrder::getOrderNo, orderNo)
                .set(UserOrder::getBillId, bill.getId()));
        return RestResponse.ok();
    }

    @GetMapping("/getByOrderNo")
    public RestResponse getByOrderNo(@RequestParam("orderNo") String orderNo) {

        return RestResponse.ok(billService.getOne(new QueryWrapper<Bill>().lambda().eq(Bill::getOrderNo, orderNo)));
    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") String id) {

        return RestResponse.ok(billService.getById(id));
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Bill bill) {

        billService.updateById(bill);
        return RestResponse.ok();
    }
}
