package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.entity.QueryCompanyResult;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;

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
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

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
    @Resource
    private UserService userService;
    private static final CloseableHttpClient httpclient = HttpClients.createDefault();
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";

    private static final String appkey = "96d04ac2d59d46ffbfa79eb6af6e69d5";
    private static final String secretKey = "E3A1E9ECD707219FAA04C125DF7A791B";


    /**
     * 查询企业信息
     *
     * @return
     */

    @GetMapping("/getCompany")
    public RestResponse getCompany(@RequestParam("name") String name) {

        User myUSer = userService.getById(SecurityUtils.getUser().getId());
        if (myUSer.getQueryCompanyCount() <= 0) {
            return RestResponse.failed("今日查询次数大于10次");
        }
        userService.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId, SecurityUtils.getUser().getId())
                .set(User::getQueryCompanyCount, myUSer.getQueryCompanyCount() - 1)

        );
        String url = "https://api.qichacha.com/FuzzySearch/GetList?key=96d04ac2d59d46ffbfa79eb6af6e69d5&searchKey=" + name;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", userAgent);
        //SecretKey  E3A1E9ECD707219FAA04C125DF7A791B
        //1027
        String[] autherHeader = RandomAuthentHeader();
        httpGet.setHeader("Token", autherHeader[0]);
        httpGet.setHeader("Timespan", autherHeader[1]);
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                QueryCompanyResult queryCompanyResult = JSONObject.parseObject(result, QueryCompanyResult.class);

                log.info(result);
                return RestResponse.ok(queryCompanyResult);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return RestResponse.ok();
    }

    public static void main(String[] args) {


        String url = "https://api.qichacha.com/FuzzySearch/GetList?key=96d04ac2d59d46ffbfa79eb6af6e69d5&searchKey=" + "易网健";
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", userAgent);
        //SecretKey  E3A1E9ECD707219FAA04C125DF7A791B
        //1027
        String[] autherHeader = RandomAuthentHeader();
        httpGet.setHeader("Token", autherHeader[0]);
        httpGet.setHeader("Timespan", autherHeader[1]);
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                QueryCompanyResult queryCompanyResult = JSONObject.parseObject(result, QueryCompanyResult.class);

                log.info(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 获取Auth Code
    protected static final String[] RandomAuthentHeader() {
        String timeSpan = String.valueOf(System.currentTimeMillis() / 1000);
        String[] authentHeaders = new String[]{DigestUtils.md5Hex(appkey.concat(timeSpan).concat(secretKey)).toUpperCase(), timeSpan};
        return authentHeaders;
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
