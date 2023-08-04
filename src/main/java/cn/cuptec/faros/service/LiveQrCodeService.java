package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.LiveQrCodeMapper;
import cn.hutool.core.net.url.UrlBuilder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Service
public class LiveQrCodeService extends ServiceImpl<LiveQrCodeMapper, LiveQrCode> {

    @Resource
    private ProductStockService productStockService;
    @Resource
    private ProductStockRepairRecordService productStockRepairRecordService;
    @Resource
    private ProductService productService;
    @Resource
    private DoctorEditInfoService doctorEditInfoService;
    @Resource
    private ProductStockRelationQrCodeService productStockRelationQrCodeService;
    @Resource
    private OperationRecordService operationRecordService;
    @Resource
    private UserService userService;
    private final Url urlData;

    /**
     * 根据活码id获取其调度url
     *
     * @param id
     * @return
     */
    public String getFullDispatcherUrl(String id) {
        return urlData.getUrl() + QrCodeConstants.DISPATCHER_URL + id;
    }

    public String getNaliFullDispatcherUrl(Integer id) {
        return urlData.getUrl() + QrCodeConstants.DISPATCHER_NALI_URL + id;
    }

    public IPage<LiveQrCode> page(IPage<LiveQrCode> page, String productSn, String mac) {
        IPage<LiveQrCode> liveQrCodeIPage = baseMapper.pageQrcodeSearch(page, productSn, mac);
        List<LiveQrCode> records = liveQrCodeIPage.getRecords();

        records.forEach(it -> {

            if (it.getProductSn() == null) {
                it.setBindStatus(false);
            } else {
                it.setBindStatus(true);
            }
        });
        liveQrCodeIPage.setRecords(records);
        return liveQrCodeIPage;
    }

    public IPage<LiveQrCode> pageNali(IPage<LiveQrCode> page, String productSn, String mac) {
        IPage<LiveQrCode> liveQrCodeIPage = baseMapper.pageNaliQrcodeSearch(page, productSn, mac);
        List<LiveQrCode> records = liveQrCodeIPage.getRecords();
        records.forEach(it -> {
            if (it.getProductSn() == null) {
                it.setBindStatus(false);
            } else {
                it.setBindStatus(true);
            }
        });
        liveQrCodeIPage.setRecords(records);
        return liveQrCodeIPage;
    }

    public IPage<LiveQrCode> pageHxdProductQrcode(IPage<LiveQrCode> page, String productSn, String mac) {
        IPage<LiveQrCode> liveQrCodeIPage = baseMapper.pageHxdProductQrcode(page, productSn, mac);
        List<LiveQrCode> records = liveQrCodeIPage.getRecords();
        records.forEach(it -> {
            if (it.getProductSn() == null) {
                it.setBindStatus(false);
            } else {
                it.setBindStatus(true);
            }
        });
        liveQrCodeIPage.setRecords(records);
        return liveQrCodeIPage;
    }

    @Override
    public boolean updateById(LiveQrCode liveQrCode) {
        ProductStock productStock = productStockService.getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getLiveQrCodeId, liveQrCode.getId()));
        User byId = userService.getById(liveQrCode.getUserId());
        //添加编辑记录
        ProductStockRepairRecord record = new ProductStockRepairRecord();
        record.setCreateTime(new Date());
        record.setProductStockId(productStock.getId());
        record.setContent("用户ID" + byId.getNickname() + "" + byId.getPhone() + "编辑mac地址从" + productStock.getMacAddress() + "改成" + liveQrCode.getMacAddress() + "序列号从" +
                productStock.getProductSn() + "改为" + liveQrCode.getProductSn());
        productStockRepairRecordService.save(record);
        productStock.setProductSn(liveQrCode.getProductSn());
        productStock.setMacAddress(liveQrCode.getMacAddress());
        productStock.setProductId(liveQrCode.getProductId());
        productStockService.updateById(productStock);
        return Boolean.TRUE;
    }

    public boolean superUpdateById(LiveQrCode liveQrCode) {
        super.updateById(liveQrCode);
        return Boolean.TRUE;
    }

    public Boolean add(LiveQrCode entity) {
        return super.save(entity);
    }


    @Transactional
    public ProductStock saveProductStock(LiveQrCode entity) {
        if (!StringUtils.isEmpty(entity.getProductSn())) {


            ProductStock dbProductStock = productStockService.getOne(Wrappers.<ProductStock>lambdaQuery()
                    .nested(query -> query.eq(ProductStock::getProductSn, entity.getProductSn()).eq(ProductStock::getDel, 1))
                    .or(query -> query.eq(ProductStock::getMacAddress, entity.getMacAddress()).eq(ProductStock::getDel, 1)).last(" limit 1"));
            if (dbProductStock != null) {
                throw new RuntimeException("已存在序列号为【" + dbProductStock.getProductSn() + "】的产品 或者 mac地址为 [" + dbProductStock.getMacAddress() + "] ");
            }
        }
        super.save(entity);
        ProductStock productStock = new ProductStock();

        if (!StringUtils.isEmpty(entity.getProductSn())) {
            //生成设备激活码
            productStock.setProductProductionDate(entity.getProductProductionDate());
            productStock.setProductSn(entity.getProductSn());
            productStock.setProductId(entity.getProductId());
            productStock.setMacAddress(entity.getMacAddress());
            productStock.setStatus(0);
            productStock.setLiveQrCodeId(entity.getId());
            productStock.setIccId(entity.getIccId());
            productStock.setVersionStr(entity.getVersionStr());
            productStock.setSystemVersion(entity.getSystemVersion());
            productStock.setCurrentUserId(entity.getCurrentUserId());
            productStock.setIpAdd(entity.getIpAdd());
            productStockService.save(productStock);
            Date productProductionDate = entity.getProductProductionDate();
            Instant instant = productProductionDate.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDate start = instant.atZone(zoneId).toLocalDate();
            LocalDate date = start.plusYears(10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String endTime = formatter.format(date);
            String startTime = formatter.format(start);
            productStockService.initActivationCode(entity.getMacAddress(), endTime, 0, startTime, productStock.getId());
        }
        return productStock;
    }

    public boolean superSave(LiveQrCode liveQrCode) {

        return super.save(liveQrCode);
    }

    public boolean delete(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        LiveQrCode liveQrCode = getById(id);
        Assert.notNull(liveQrCode, "二维码不存在，请刷新后重新操作");
        ProductStock productStock = productStockService.getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getLiveQrCodeId, liveQrCode.getId()));
        if (productStock != null) {
            //添加操作记录
            OperationRecord operationRecord = new OperationRecord();
            operationRecord.setPathUrl("productStock/deleteById");
            operationRecord.setText("删除设备");
            operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
            operationRecord.setCreateTime(new Date());
            operationRecordService.save(operationRecord);

            productStockService.unbindQrcode(productStock.getLiveQrCodeId());
        }
        return super.removeById(id);
    }

    public IPage pageScoped(IPage page, Wrapper wrapper) {
        IPage pageResult = baseMapper.pageScoped(page, wrapper, new DataScope());
        return pageResult;
    }

    public List<LiveQrCode> listScoped(Wrapper wrapper) {
        return baseMapper.listScoped(wrapper, new DataScope());
    }

    public void dispatcherNaLi(Integer productStockId) throws IOException {
        //跳转地址
        ServletUtils.getResponse().sendRedirect(urlData.getUrl() + QrCodeConstants.NALI_URL + "?id=" + productStockId);
    }

    //活码调度
    public void dispatcher(String qrCodeId) throws IOException {
        LiveQrCode liveQrCode = getById(qrCodeId);
        //查询产品
        ProductStock productStock = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getLiveQrCodeId, qrCodeId));
        //跳转h5中转页面 判断用户是否关注公众号
        String servicePackId = productStock.getServicePackId();
        if (!StringUtils.isEmpty(servicePackId)) {
            log.info("获取的服务包id===========:{}", servicePackId);
            String url = urlData.getUrl() + "index.html#/nali/redBean?id=" + servicePackId + "&macAdd=" + productStock.getMacAddress();
            ServletUtils.getResponse().sendRedirect(url);
        } else {
            toIntroduce(productStock);
        }
    }


    private void toIntroduce(ProductStock productStock) {
        Product product = productService.getById(productStock.getProductId());
        if (product.getId().equals(2)) {
            //如果是家庭版跳转到公众号首页
            try {
                ServletUtils.getResponse().sendRedirect("https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=" + urlData.getBiz() + "&scene=117#wechat_redirect");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (product.getId() == 1 || product.getId() == 101) {
            //下肢
            try {
                ServletUtils.getResponse().sendRedirect(urlData.getUrl() + "index.html#/pneumaticDevice?type=2&macAdd=" + productStock.getMacAddress());
                //ServletUtils.getResponse().sendRedirect("https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz="+urlData.getBiz()+"&scene=117#wechat_redirect");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //气动
            try {
                //ServletUtils.getResponse().sendRedirect("https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=MzkwNDQyMzI1NQ==&scene=117#wechat_redirect");

                ServletUtils.getResponse().sendRedirect(urlData.getUrl() + "index.html#/pneumaticDevice?type=1&macAdd=" + productStock.getMacAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
