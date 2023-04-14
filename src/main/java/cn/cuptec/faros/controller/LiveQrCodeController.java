package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.config.wx.builder.TextBuilder;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@RestController
@RequestMapping("/liveQrCode")
public class LiveQrCodeController extends AbstractBaseController<LiveQrCodeService, LiveQrCode> {

    @Resource
    private LiveQrCodeService liveQrCodeService;
    @Resource
    private ProductService productService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private BrandService brandService;
    @Resource
    private ProductStockRelationQrCodeService productStockRelationQrCodeService;
    @Resource
    private UserService userService;
    @Resource
    private WxMpService wxMpService;
    private final Url url;

    @GetMapping("/dispatcher/{id}")
    public void dispatcher(@PathVariable String id) throws IOException {
        service.dispatcher(id);
    }

    @GetMapping("/dispatcherNaLi/{id}")
    public void dispatcherNaLi(@PathVariable Integer id) throws IOException {
        service.dispatcherNaLi(id);
    }

    @GetMapping("/getLiveQrCodeUrl")
    public RestResponse getLiveQrCodeUrl(@RequestParam("macAdd") String macAdd) {
        ProductStock one = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAdd));
        if (one == null) {
            return RestResponse.failed("没有该设备");
        }
        LiveQrCode byId = service.getById(one.getLiveQrCodeId());
        if (byId.getType() == 7) {
            if (StringUtils.isEmpty(byId.getUrl())) {
                return RestResponse.ok("https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=Mzg2NzcxMjg4Mw==&scene=110#wechat_redirect");
            }
            return RestResponse.ok(byId.getUrl());
        }
        return RestResponse.ok(url.getUrl() + "/liveQrCode/dispatcher/" + one.getLiveQrCodeId());
    }

    @GetMapping("/sendLiveQrCodeNotice")
    public void sendLiveQrCodeNotice(@RequestParam("productStockId") Integer productStockId, @RequestParam("macAddress") String macAddress, @RequestParam("servicePackageName") String servicePackageName) {
        //发送公众号消息
        User user = userService.getById(SecurityUtils.getUser().getId());
        if (user != null) {
            if (!org.apache.commons.lang3.StringUtils.isEmpty(user.getMpOpenId())) {

                String url1 =url.getUrl() +  QrCodeConstants.NALI_URL + "?id=" + productStockId + "&macAddress=" + macAddress + "&form=" + "button";
                //查询服务包名字

                wxMpService.sendLiveQrCodeNotice(user.getMpOpenId(), "您已扫码成功点击查看", servicePackageName, "易网建", "点击查看详情", url1);

                userService.updateById(user);
            }
        }

    }

    //根据mac地址发送给用户公众好购买模版消息
    @GetMapping("/sendLiveQrCodeNoticeByMacAdd")
    public void sendLiveQrCodeNoticeByMacAdd(@RequestParam("macAddress") String macAddress) {
        //发送公众号消息
        User user = userService.getById(SecurityUtils.getUser().getId());
        List<ProductStock> list = productStockService.list(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        ProductStock productStock = list.get(0);
        List<ProductStockRelationQrCode> productStockRelationQrCodes = productStockRelationQrCodeService.list(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, productStock.getId()));
        if (CollectionUtils.isEmpty(productStockRelationQrCodes)) {
            return;
        }
        ProductStockRelationQrCode productStockRelationQrCode = productStockRelationQrCodes.get(0);
        //查询服务包名字
        LiveQrCode liveQrCode = liveQrCodeService.getById(productStockRelationQrCode.getLiveQrCodeId());

        if (user != null) {
            if (!org.apache.commons.lang3.StringUtils.isEmpty(user.getMpOpenId())) {

                String url1 =url.getUrl() +  QrCodeConstants.NALI_URL + "?id=" + productStock.getId() + "&macAddress=" + macAddress + "&form=" + "button";
                //查询服务包名字

                wxMpService.sendLiveQrCodeNotice(user.getMpOpenId(), "您已扫码成功。", liveQrCode == null ? "康复设备" : liveQrCode.getName(), "易网建", "点击查看详情", url1);

                userService.updateById(user);
            }
        }

    }

    /**
     * 产品二维码列表展示
     *
     * @param
     * @return
     */
    @GetMapping("/pageProductQrcode")
    public RestResponse getLiveQrCodePage(@RequestParam(value = "productSn", required = false) String productSn,
                                          @RequestParam(value = "mac", required = false) String mac) {
        IPage page = getNullablePage();
        if (page == null) {
            return RestResponse.failed("分页参数不能为空");
        } else {
            return RestResponse.ok(service.page(page, productSn, mac));
        }
    }

    @GetMapping("/pageNaliProductQrcode")
    public RestResponse getNaliLiveQrCodePage(@RequestParam(value = "productSn", required = false) String productSn,
                                              @RequestParam(value = "mac", required = false) String mac) {
        IPage page = getNullablePage();
        if (page == null) {
            return RestResponse.failed("分页参数不能为空");
        } else {
            return RestResponse.ok(service.pageNali(page, productSn, mac));
        }
    }

    @GetMapping("/pageHxdProductQrcode")
    public RestResponse pageHxdProductQrcode(@RequestParam(value = "productSn", required = false) String productSn,
                                             @RequestParam(value = "mac", required = false) String mac) {
        IPage page = getNullablePage();
        if (page == null) {
            return RestResponse.failed("分页参数不能为空");
        } else {
            return RestResponse.ok(service.pageHxdProductQrcode(page, productSn, mac));
        }
    }

    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<LiveQrCode> page = getPage();
        if (page == null) {
            return RestResponse.failed("分页参数不能为空");
        } else {
            return RestResponse.ok(service.pageScoped(page, queryWrapper));
        }
    }

    @GetMapping("/list")
    public RestResponse list() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        return RestResponse.ok(service.listScoped(queryWrapper));

    }

    public static void main(String[] args) {
        String a = "1:1：1";
        System.out.println(a.replace("：", ":"));
    }

    /**
     * 新增二维码
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public RestResponse save(@Valid @RequestBody LiveQrCode liveQrCode) {
        if (!StringUtils.isEmpty(liveQrCode.getMacAddress())) {
            String replace = liveQrCode.getMacAddress().replace("：", ":");
            liveQrCode.setMacAddress(replace.toLowerCase());
        }
        return RestResponse.ok(service.saveProductStock(liveQrCode));
    }

    /**
     * 添加n纳里二维码
     *
     * @param
     * @return
     */
    @PostMapping("/addNaLiCode")
    public RestResponse addNaLiCode(@Valid @RequestBody LiveQrCode liveQrCode) {
        Integer id = SecurityUtils.getUser().getId();
        User byId = userService.getById(id);
        if (byId != null && byId.getDeptId() != null) {
            liveQrCode.setDeptId(byId.getDeptId());
        }

        return RestResponse.ok(service.superSave(liveQrCode));
    }

    /**
     * 删除二维码
     *
     * @param id{ return RestResponse.ok(service.save(liveQrCode));
     *            }
     * @return
     */
    @DeleteMapping("/{id}")
    public RestResponse deleteById(@PathVariable String id) {
        return service.removeById(id) ? RestResponse.ok() : RestResponse.failed("删除失败，请检查参数");
    }

    /**
     * 获取以医院或者家庭版设备二维码第一张图片
     *
     * @return
     */
    @GetMapping("/drawLogoQRCodeHospital1/{id}")
    public RestResponse drawLogoQRCodeHospital1(@PathVariable String id) throws Exception {
        LiveQrCode liveQrCode = service.getById(id);
        if (liveQrCode != null) {
            String url = liveQrCodeService.getFullDispatcherUrl(liveQrCode.getId());
            if (liveQrCode.getType() == 7) {
                ;
                url = liveQrCode.getUrl() + "/" + liveQrCode.getId();
            }

            ProductStock productStock = productStockService.getproductStockByCodeId(id);
            Product product = productService.getById(productStock.getProductId());
            Brand brand = brandService.getById(product.getBrandId());

            String logoUrl = brand.getBrandLogo();
            String note = "";
            String note3 = "";
            //1 是院内版
            if (product.getProductType() == 1) {
                BufferedImage png = QrCodeUtil.drawLogoQRCodeHospital1(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 1);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            } else {
                //家庭版
                note = "售后服务及回收请扫码";
                note3 = "服务热线:400-900-1022";
                BufferedImage png = QrCodeUtil.drawLogoQRCodeHospital1(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 2);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            }

            //  }


        } else {
            return RestResponse.failed("此ID无对应的二维码");
        }
    }

    /**
     * 获取设备二维码第二张图片
     *
     * @return
     */
    @GetMapping("/drawLogoQRCodeHospital2/{id}")
    public RestResponse createQRCodeToOutputStream2(@PathVariable String id) throws Exception {
        LiveQrCode liveQrCode = service.getById(id);
        if (liveQrCode != null) {
            String url = liveQrCodeService.getFullDispatcherUrl(liveQrCode.getId());
            if (liveQrCode.getType() == 7) {
                url = liveQrCode.getUrl() + "/" + liveQrCode.getId();
            }
            ProductStock productStock = productStockService.getproductStockByCodeId(id);
            Product product = productService.getById(productStock.getProductId());
            Brand brand = brandService.getById(product.getBrandId());

            String logoUrl = brand.getBrandLogo();
            String note = "";
            String note3 = "";
            if (product.getProductType() == 1) {
                BufferedImage png = QrCodeUtil.drawLogoQRCodeHospital2(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 1);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            } else {
                //家庭版
                note = "售后服务及回收请扫码";
                note3 = "服务热线:400-900-1022";
                BufferedImage png = QrCodeUtil.drawLogoQRCodeHospital2(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 2);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            }


        } else {
            return RestResponse.failed("此ID无对应的二维码");
        }
    }

    /**
     * 根据id获取二维码
     *
     * @return
     */
    @GetMapping("/qrcodeType/{id}")
    public RestResponse createQRCodeToOutputStream1(@PathVariable String id) throws Exception {
        LiveQrCode liveQrCode = service.getById(id);
        if (liveQrCode != null) {
            String url = liveQrCodeService.getFullDispatcherUrl(liveQrCode.getId());
            if (liveQrCode.getType() == 1) {

                ProductStock productStock = productStockService.getproductStockByCodeId(id);
                Product product = productService.getById(productStock.getProductId());
                Brand brand = brandService.getById(product.getBrandId());

                String logoUrl = brand.getBrandLogo();
                String note = "";
                String note3 = "";
                String note4 = "";
                if (product.getProductType() == 1) {
                    note = "售后服务及回收请扫码";
                    note3 = "全国售后服务:400-900-1022";
                    note4 = "维修公众号:易网健";
                } else {
                    note = "售后服务及回收请扫码";
                    note3 = "售后服务:400-900-1022";
                }
                BufferedImage png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 1);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            } else {
                ProductStock productStock = productStockService.getproductStockByCodeId(id);
                Product product = productService.getById(productStock.getProductId());
                Brand brand = brandService.getById(product.getBrandId());

                String logoUrl = brand.getBrandLogo();
                String note = "";
                String note3 = "";
                if (product.getProductType() == 1) {
                    note = "售后服务及回收请扫码";
                    note3 = "全国售后服务:400-900-1022";
                } else {
                    note = "售后服务及回收请扫码";
                    note3 = "售后服务:400-900-1022";
                }

                BufferedImage png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 1);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            }


        } else {
            return RestResponse.failed("此ID无对应的二维码");
        }
    }

    /**
     * 根据id获取二维码
     *
     * @return
     */
    @GetMapping("/qrcode/{id}")
    public RestResponse createQRCodeToOutputStream(@PathVariable String id) throws Exception {
        LiveQrCode liveQrCode = service.getById(id);
        if (liveQrCode != null) {
            String url = liveQrCodeService.getFullDispatcherUrl(liveQrCode.getId());
            if (liveQrCode.getType() == 1) {

                ProductStock productStock = productStockService.getproductStockByCodeId(id);
                Product product = productService.getById(productStock.getProductId());
                Brand brand = brandService.getById(product.getBrandId());

                String logoUrl = brand.getBrandLogo();
                String note = "";
                String note3 = "";
                String note4 = "";
                if (product.getProductType() == 1) {
                    note = "售后服务及回收请扫码";
                    note3 = "全国售后服务:400-900-1022";
                    note4 = "维修公众号:易网健";
                } else {
                    note = "售后服务及回收请扫码";
                    note3 = "售后服务:400-900-1022";
                }
                BufferedImage png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 2);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            } else {
                ProductStock productStock = productStockService.getproductStockByCodeId(id);
                Product product = productService.getById(productStock.getProductId());
                Brand brand = brandService.getById(product.getBrandId());

                String logoUrl = brand.getBrandLogo();
                String note = "";
                String note3 = "";
                if (product.getProductType() == 1) {
                    note = "售后服务及回收请扫码";
                    note3 = "全国售后服务:400-900-1022";
                } else {
                    note = "售后服务及回收请扫码";
                    note3 = "售后服务:400-900-1022";
                }

                BufferedImage png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, note3, 2);
                return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
            }


        } else {
            return RestResponse.failed("此ID无对应的二维码");
        }
    }


    @GetMapping("/qrcodeByMac/{macAddress}")
    public RestResponse createQRCodeToOutputStreamByMacAddress(@PathVariable String macAddress) throws Exception {
        ProductStock one = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress).eq(ProductStock::getDel, 1));
        if (one == null)
            throw new RuntimeException("未注册设备");
        return createQRCodeToOutputStream(one.getLiveQrCodeId());
    }

    @PutMapping("updateProductQrcode")
    public RestResponse updateProductSn(@RequestBody LiveQrCode liveQrCode) {
        //判断mac地址是否重复


        if (StringUtils.isNotEmpty(liveQrCode.getMacAddress())) {


            liveQrCode.setMacAddress(liveQrCode.getMacAddress().toLowerCase());
            List<ProductStock> dbProductStock = productStockService.list(Wrappers.<ProductStock>lambdaQuery()
                    .nested(query -> query.eq(ProductStock::getProductSn, liveQrCode.getProductSn()).eq(ProductStock::getDel, 1))
                    .or(query -> query.eq(ProductStock::getMacAddress, liveQrCode.getMacAddress()).eq(ProductStock::getDel, 1)));
            if (!CollectionUtils.isEmpty(dbProductStock)) {
                for (ProductStock productStock : dbProductStock) {
                    if (productStock != null && !productStock.getId().equals(liveQrCode.getProductStockId())) {
                        throw new RuntimeException("已存在序列号为【" + productStock.getProductSn() + "】的产品 或者 mac地址为 [" + productStock.getMacAddress() + "] ");
                    }

                }
            }

        }

        service.superUpdateById(liveQrCode);
        liveQrCode.setUserId(SecurityUtils.getUser().getId());
        service.updateById(liveQrCode);
        return RestResponse.ok();
    }

    @PutMapping("update")
    public RestResponse update(@RequestBody LiveQrCode liveQrCode) {

        service.superUpdateById(liveQrCode);
        return RestResponse.ok();
    }

    @GetMapping("/checkGetQrCodeByProductStockId")
    public RestResponse checkGetQrCodeByProductStockId(@RequestParam("productStockId") Integer productStockId) throws Exception {
        //判断是否绑定纳里二维码
        List<ProductStockRelationQrCode> list = productStockRelationQrCodeService.list(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, productStockId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.failed("暂无绑定纳里健康二维码");
        }
        return RestResponse.ok();
    }

    /**
     * 根据设备id获取二维码 用于纳里二维码跳转
     *
     * @return
     */
    @GetMapping("/getQrCodeByProductStockId")
    public RestResponse getQrCodeByProductStockId(@RequestParam("productStockId") Integer productStockId) throws Exception {
        //判断是否绑定纳里二维码
        List<ProductStockRelationQrCode> list = productStockRelationQrCodeService.list(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, productStockId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.failed("暂无绑定纳里健康二维码");
        }
        String url = liveQrCodeService.getNaliFullDispatcherUrl(productStockId);
        ProductStock productStock = productStockService.getById(productStockId);
        Product product = productService.getById(productStock.getProductId());
        Brand brand = brandService.getById(product.getBrandId());

        String logoUrl = brand.getBrandLogo();
        String note = "";

        if (product.getProductType() == 1) {
            note = "售后服务及回收请扫码";
        } else {
            note = "售后服务及回收请扫码";
        }
        BufferedImage png = QrCodeUtil.drawLogoQRCode(ServletUtils.getResponse().getOutputStream(), "png", logoUrl, url, note, 300, null, 2);
        return png != null ? RestResponse.ok() : RestResponse.failed("生成二维码失败");
    }


    @Override
    protected Class<LiveQrCode> getEntityClass() {
        return LiveQrCode.class;
    }
}
