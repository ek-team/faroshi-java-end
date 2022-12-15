package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.ElectronicInvoiceService;
import cn.cuptec.faros.service.NNAccountService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import nuonuo.open.sdk.NNOpenSDK;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 电子发票
 */
@RestController
@RequestMapping("/electronicInvoice")
@AllArgsConstructor
public class ElectronicInvoiceController extends AbstractBaseController<ElectronicInvoiceService, ElectronicInvoice> {
    private final ElectronicInvoiceService electronicInvoiceService;
    private final String TAX_NUM = "TAX_NUM";
    private final StringRedisTemplate redisTemplate;
    @Resource
    private NNAccountService nnAccountService;

    /**
     * 通过code换取access_token
     *
     * @return
     */
    @GetMapping("getAccessToken")
    public RestResponse getAccessToken(@RequestParam("taxNum") String taxNum, @RequestParam("code") String code) {

        return RestResponse.ok(electronicInvoiceService.getAccessToken(taxNum, code));
    }

    /**
     * 添加发票信息
     *
     * @return
     */
    @ApiOperation(value = "添加发票信息")
    @PostMapping("add")
    public RestResponse<Boolean> save(@RequestBody ElectronicInvoice electronicInvoice) {
        return RestResponse.ok(electronicInvoiceService.save(electronicInvoice));
    }

    @GetMapping("/page")
    public RestResponse<IPage> page(@RequestParam("salesmanId") int salesmanId) {
        Page<ElectronicInvoice> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("salesman_id", salesmanId);

        if (page != null)
            return RestResponse.ok(electronicInvoiceService.page(page, queryWrapper));
        else
            return RestResponse.ok();
    }

    @PutMapping("/updateById")
    public RestResponse updateById(@RequestBody ElectronicInvoice electronicInvoice) {

        return RestResponse.ok(service.updateById(electronicInvoice));
    }

    @ApiOperation(value = "申请开发票 调用诺诺平台")
    @PostMapping("invoicing")
    public RestResponse<Boolean> invoicing(@RequestBody ElectronicInvoice electronicInvoice) {

        return RestResponse.ok();
    }

    @ApiOperation(value = "查询开票状态")
    @GetMapping("queryStatus")
    public RestResponse<String> queryStatus(@RequestParam("id") int id) {
        ElectronicInvoice electronicInvoice = electronicInvoiceService.getById(id);
        if (electronicInvoice == null) {
            return RestResponse.failed("id不存在");
        }
        Integer salesmanId = electronicInvoice.getSalesmanId();
        NNAccount nnAccount = nnAccountService.getOne(Wrappers.<NNAccount>lambdaQuery().eq(NNAccount::getUId, salesmanId));
        if (nnAccount == null) {
            return RestResponse.failed("商户不存在");
        }
        NNOpenSDK sdk = NNOpenSDK.getIntance();
        String taxnum = nnAccount.getTaxNum(); // 授权企业税号
        String appKey = nnAccount.getAppKey();
        String appSecret = nnAccount.getAppSecret();
        String method = "nuonuo.ElectronInvoice.queryInvoiceResult"; // API方法名
        String token = redisTemplate.opsForValue().get(TAX_NUM + nnAccount.getUId());
        ; // 访问令牌
        String url = "https://sdk.nuonuo.com/open/v1/services"; // SDK请求地址
        ElectronicInvoiceQueryStatusParam param = new ElectronicInvoiceQueryStatusParam();
        String content = JSONObject.toJSONString(param);
        String senid = UUID.randomUUID().toString().replace("-", ""); // 唯一标识，32位随机码，无需修改，保持默认即可
        String result = sdk.sendPostSyncRequest(url, senid, appKey, appSecret, token, taxnum, method, content);
        System.out.println(result);
        return RestResponse.ok(result);
    }

    @Override
    protected Class<ElectronicInvoice> getEntityClass() {
        return ElectronicInvoice.class;
    }
}
