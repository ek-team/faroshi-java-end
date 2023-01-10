//package cn.cuptec.faros.controller;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.github.binarywang.wxpay.bean.media.ImageUploadResult;
//import com.github.binarywang.wxpay.config.WxPayConfig;
//import com.github.binarywang.wxpay.exception.WxPayException;
//import com.github.binarywang.wxpay.service.MerchantMediaService;
//import com.github.binarywang.wxpay.service.WxPayService;
//import com.github.binarywang.wxpay.service.impl.MerchantMediaServiceImpl;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * 支付进件申请单
// *
// * @author www.joolun.com
// * @date 2020-08-27 10:51:37
// */
//@Slf4j
//@RestController
//@AllArgsConstructor
//@RequestMapping("/payapplyform")
//@Api(value = "payapplyform", tags = "支付进件申请单管理")
//public class PayApplyFormController {
//
//    private final PayApplyFormService payApplyFormService;
//
//    /**
//     * 分页列表
//     * @param page 分页对象
//     * @param payApplyForm 支付进件申请单
//     * @return
//     */
//    @ApiOperation(value = "分页列表")
//    @GetMapping("/page")
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:index')")
//    public R getPage(Page page, PayApplyForm payApplyForm) {
//        return R.ok(payApplyFormService.page(page, Wrappers.query(payApplyForm)));
//    }
//
//    /**
//     * 支付进件申请单查询
//     * @param id
//     * @return R
//     */
//    @ApiOperation(value = "支付进件申请单查询")
//    @GetMapping("/{id}")
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:get')")
//    public R getById(@PathVariable("id") String id) {
//        return R.ok(payApplyFormService.getById(id));
//    }
//
//    /**
//     * 支付进件申请单新增
//     * @param payApplyForm 支付进件申请单
//     * @return R
//     */
//    @ApiOperation(value = "支付进件申请单新增")
//    @SysLog("新增支付进件申请单")
//    @PostMapping
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:add')")
//    public R save(@RequestBody PayApplyForm payApplyForm) {
//        return R.ok(payApplyFormService.save(payApplyForm));
//    }
//
//    /**
//     * 支付进件申请单修改
//     * @param payApplyForm 支付进件申请单
//     * @return R
//     */
//    @ApiOperation(value = "支付进件申请单修改")
//    @SysLog("修改支付进件申请单")
//    @PutMapping
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:edit')")
//    public R updateById(@RequestBody PayApplyForm payApplyForm) {
//        return R.ok(payApplyFormService.updateById(payApplyForm));
//    }
//
//    /**
//     * 支付进件申请单删除
//     * @param id
//     * @return R
//     */
//    @ApiOperation(value = "支付进件申请单删除")
//    @SysLog("删除支付进件申请单")
//    @DeleteMapping("/{id}")
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:del')")
//    public R removeById(@PathVariable String id) {
//        return R.ok(payApplyFormService.removeById(id));
//    }
//
//    /**
//     * 支付进件申请单提交
//     * @param payApplyForm 支付进件申请单
//     * @return R
//     */
//    @ApiOperation(value = "支付进件申请单提交")
//    @SysLog("支付进件申请单提交")
//    @PostMapping("/submit")
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:submit')")
//    public R submit(@RequestBody PayApplyForm payApplyForm) throws WxPayException {
//        return R.ok(payApplyFormService.submit(payApplyForm));
//    }
//
//    /**
//     * 查询申请状态
//     * @param payApplyForm 查询申请状态
//     * @return R
//     */
//    @ApiOperation(value = "查询申请状态")
//    @SysLog("查询申请状态")
//    @PostMapping("/applystatus")
//    @PreAuthorize("@ato.hasAuthority('payapi:payapplyform:submit')")
//    public R queryApplyStatus(@RequestBody PayApplyForm payApplyForm) {
//        payApplyFormService.queryApplyStatus(payApplyForm);
//        return R.ok();
//    }
//
//    /**
//     * 通用接口-图片上传API
//     * @param mulFile
//     * @return
//     */
//    @ApiOperation(value = "通用接口-图片上传API")
//    @PostMapping("/imageUploadV3")
//    public R imageUploadV3(@RequestParam("file") MultipartFile mulFile) throws IOException, WxPayException {
//        WxPayConfig wxPayConfig = new WxPayConfig();
//        WxPayService wxPayService = WxPayConfiguration.getPayService(wxPayConfig);
//        MerchantMediaService merchantMediaService = new MerchantMediaServiceImpl(wxPayService);
//        File file = FileUtils.multipartFileToFile(mulFile);
//        ImageUploadResult imageUploadResult = merchantMediaService.imageUploadV3(file);
//        return R.ok(imageUploadResult);
//    }
//}
