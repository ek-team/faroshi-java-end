package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.entity.ElectronicInvoice;
import cn.cuptec.faros.entity.NNAccount;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.mapper.ElectronicInvoiceMapper;
import cn.cuptec.faros.mapper.NNAccountMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import nuonuo.open.sdk.NNOpenSDK;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ElectronicInvoiceService extends ServiceImpl<ElectronicInvoiceMapper, ElectronicInvoice> {
    private final String TAX_NUM = "TAX_NUM";
    private final StringRedisTemplate redisTemplate;

    @Resource
    private NNAccountService nnAccountService;
    public String getAccessToken(String taxNum, String code) {
        String token = redisTemplate.opsForValue().get(TAX_NUM);
        if (!StringUtils.isEmpty(token)) {
            return token;
        }
        //查询商户的信息
        NNAccount nnAccount = nnAccountService.getOne(Wrappers.<NNAccount>lambdaQuery().eq(NNAccount::getTaxNum, taxNum));
        if(nnAccount!=null){
            NNOpenSDK sdk = NNOpenSDK.getIntance();
            String tokenData = sdk.getIntance().getISVToken(nnAccount.getAppKey(), nnAccount.getAppSecret(), code, taxNum, "redirectUri");
            redisTemplate.opsForValue().set(TAX_NUM+nnAccount.getUId(), "token", 1l, TimeUnit.SECONDS);
        }

        return token;
    }
}
