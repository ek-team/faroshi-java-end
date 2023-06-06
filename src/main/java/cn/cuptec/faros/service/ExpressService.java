package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.sms.HttpUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Express;
import cn.cuptec.faros.entity.RetrieveOrder;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.mapper.ExpressMapper;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ExpressService extends ServiceImpl<ExpressMapper, Express> {

    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private RetrieveOrderService retrieveOrderService;

    //快递100
//    private static String customer = "85D6BC13E972F4D077748A8DF0768C2A";
//    private static String key = "kpDqiQJf634";
    //快递100
    private static String customer = "94F96E20C049C1232DBF02B729F710B0";
    private static String key = "JAnUGrLl5945";
    /**
     * 获取用户订单物流轨迹信息
     *
     * @param id 订单id
     * @return
     */
    public MapExpressTrackVo getUserOrderMapTrace(int id) {
        UserOrder userOrder = userOrdertService.getById(id);
        Assert.isTrue(userOrder != null, "未查询到此订单");
        MapExpressTrackVo mapExpressTrackVo = queryExpressData(userOrder.getReceiverPhone(), userOrder.getDeliveryCompanyCode(), userOrder.getDeliverySn());
        mapExpressTrackVo.setUserOrder(userOrder);
        return mapExpressTrackVo;
    }

    //获取用户回收单物流信息
    public MapExpressTrackVo queryRetrieveOrderExpressInfo(int id) {
        RetrieveOrder retrieveOrder = retrieveOrderService.getById(id);
        Assert.isTrue(retrieveOrder != null, "未查询到此订单");
        Assert.isTrue(StringUtils.isNotEmpty(retrieveOrder.getDeliverySn()), "未查询到物流信息");
        MapExpressTrackVo mapExpressTrackVo = queryExpressData(retrieveOrder.getReceiverPhone(), retrieveOrder.getDeliveryCompanyCode(), retrieveOrder.getDeliverySn());
        mapExpressTrackVo.setUserOrder(retrieveOrder);
        return mapExpressTrackVo;
    }


    private MapExpressTrackVo queryMapTrace(String com, String num, String from, String to) {
        MapExpressTrackVo.ExpressParam expressParam = new MapExpressTrackVo.ExpressParam();
        expressParam.setCom(com);
        expressParam.setNum(num);
        expressParam.setFrom(from);
        expressParam.setTo(to);
        String expressParamStr = JSON.toJSONString(expressParam);
        String signOrigion = expressParamStr + key + customer;
        String sign = DigestUtils.md5Hex(signOrigion).toUpperCase();

        String url = "https://poll.kuaidi100.com/poll/maptrack.do?customer=" + customer + "&sign= " + sign + "&param=" + expressParamStr;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("customer", customer);
        paramMap.put("sign", sign);
        paramMap.put("param", expressParamStr);
        try {
            HttpResponse response = HttpUtils.doGet("https://poll.kuaidi100.com", "/poll/maptrack.do", "get", new HashMap<>(), paramMap);
            String expressResult = EntityUtils.toString(response.getEntity()); //输出json
            MapExpressTrackVo expressTrackVo = JSON.parseObject(expressResult, MapExpressTrackVo.class);
            if (expressTrackVo.getStatus() == 200) {
                return expressTrackVo;
            } else {
                throw new RuntimeException("物流公司返回结果："+expressTrackVo.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("物流公司返回结果："+e.getMessage());
        }
    }

    /**
     * 查询物流信息
     *
     * @param com 快递公司编码
     * @param num 快递单号
     * @return
     */
    private MapExpressTrackVo queryExpressData(String phone, String com, String num) {
        MapExpressTrackVo.ExpressParam expressParam = new MapExpressTrackVo.ExpressParam();
        expressParam.setCom(com);
        expressParam.setNum(num);
        if (!StringUtils.isEmpty(phone)) {
            expressParam.setPhone(phone);

        }
        String expressParamStr = JSON.toJSONString(expressParam);
        String signOrigion = expressParamStr + key + customer;
        String sign = DigestUtils.md5Hex(signOrigion).toUpperCase();

        String url = "https://poll.kuaidi100.com/poll/maptrack.do?customer=" + customer + "&sign= " + sign + "&param=" + expressParamStr;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("customer", customer);
        paramMap.put("sign", sign);
        paramMap.put("param", expressParamStr);
        try {
            HttpResponse response = HttpUtils.doGet("https://poll.kuaidi100.com", "/poll/query.do", "get", new HashMap<>(), paramMap);
            String expressResult = EntityUtils.toString(response.getEntity()); //输出json
            log.info(expressResult + "ppppppppppppppppppppppppppppppp");
            MapExpressTrackVo expressTrackVo = JSON.parseObject(expressResult, MapExpressTrackVo.class);
            if (expressTrackVo.getStatus() == null) {
                throw new RuntimeException("物流公司返回结果："+expressTrackVo.getMessage());
            }
            if (expressTrackVo.getStatus() == 200) {
                return expressTrackVo;
            } else {
                throw new RuntimeException("物流公司返回结果："+expressTrackVo.getMessage());
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("物流公司返回结果："+e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("物流公司返回结果："+e.getMessage());
        }
    }

    public static void main(String[] args) {
        MapExpressTrackVo.ExpressParam expressParam = new MapExpressTrackVo.ExpressParam();
        expressParam.setCom("shunfeng");
        expressParam.setNum("279646798813");
        expressParam.setPhone("13862406341");
        String expressParamStr = JSON.toJSONString(expressParam);
        String signOrigion = expressParamStr + key + customer;
        String sign = DigestUtils.md5Hex(signOrigion).toUpperCase();

        String url = "https://poll.kuaidi100.com/poll/maptrack.do?customer=" + customer + "&sign= " + sign + "&param=" + expressParamStr;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("customer", customer);
        paramMap.put("sign", sign);
        paramMap.put("param", expressParamStr);

        System.out.println(paramMap);
        HttpResponse response = null;
        try {
            response = HttpUtils.doGet("https://poll.kuaidi100.com", "/poll/query.do", "post", new HashMap<>(), paramMap);
            String expressResult = EntityUtils.toString(response.getEntity()); //输出json
            log.info(expressResult + "ppppppppppppppppppppppppppppppp");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
