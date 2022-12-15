package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.dto.ContactReqDto;
import cn.cuptec.faros.common.utils.sms.SmsUtil;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.TbSms;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
public class SmsController extends AbstractBaseController<SmsService, TbSms> {

    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;
    @Value("${sms.signName}")
    private String signName;

    @Resource
    private MobileService mobileService;
    @Resource
    private UserService userService;
    @Resource
    private ProductService productService;

    /**
     * 获取登录验证码
     * @param phoneNo
     * @return
     */
    @GetMapping("/login-code")
    public RestResponse loginCode(@RequestParam String phoneNo){
        mobileService.sendLoginSmsCode(phoneNo);
        return RestResponse.ok();
    }

    /**
     * 短信记录列表，分页管理
     * @param
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageList(){
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<TbSms> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    /**
     * 获取当前用户的记录信息
     * @return
     */
    @GetMapping ("/getSmsRecordList/{id}")
    public RestResponse getSmsRecord(@PathVariable int id){
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("sale_id",id);
        Page<TbSms> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));

    }

    /**
     * 发送短信信息
     * @param
     * @return
     */

    @PostMapping ("/contactReq")
    public RestResponse sendSms(@RequestBody ContactReqDto contactReqDto) {
        //获取当前登录用户
        Integer productId = contactReqDto.getProductId();
        Integer salesmanId = contactReqDto.getSalesmanId();
        String userName = contactReqDto.getUsername();
        String userPhone = contactReqDto.getUserPhone();

        User saleMan;
        if (salesmanId != null){
            saleMan = userService.getById(salesmanId);
        }else {
            saleMan = new User();
            saleMan.setPhone("15316506176");
            saleMan.setNickname("Rason");
        }

        Product product = productService.getById(productId);
        String templateCode = "SMS_174165527";
        Map<String, String> msgMap =  new HashMap<>();
        msgMap.put("name",saleMan.getNickname());
        msgMap.put("phone",userPhone);
        SmsUtil.sendSms(accessKeyId,accessKeySecret,signName,saleMan.getPhone(),templateCode,msgMap);

        //发送成功需要把短信记录的信息保存下来
        TbSms tbSms = new TbSms();
        tbSms.setCreateDate(new Date());
        tbSms.setProductName(product.getProductName());
        tbSms.setProductId(product.getId());
        tbSms.setSaleName(saleMan.getNickname());
        tbSms.setSmsText("短信发送给"+userName+"手机号是"+userPhone);
        tbSms.setUserName(userName);
        tbSms.setUserPhone(userPhone);
        service.save(tbSms);
        return RestResponse.ok("联系成功!");
    }

    @Override
    protected Class<TbSms> getEntityClass() {
        return TbSms.class;
    }
}
