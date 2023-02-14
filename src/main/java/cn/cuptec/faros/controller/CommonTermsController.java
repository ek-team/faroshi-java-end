package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Address;
import cn.cuptec.faros.entity.CommonTerms;
import cn.cuptec.faros.service.CommonTermsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 常用语
 */
@RestController
@RequestMapping("/commonTerms")
public class CommonTermsController {
    @Resource
    private CommonTermsService commonTermsService;

    /**
     * 添加
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody CommonTerms commonTerms) {
        commonTerms.setUserId(SecurityUtils.getUser().getId());
        commonTermsService.save(commonTerms);
        return RestResponse.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public RestResponse update(@RequestBody CommonTerms commonTerms) {
        commonTermsService.updateById(commonTerms);
        return RestResponse.ok();
    }

    /**
     * 删除
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") Integer id) {
        commonTermsService.removeById(id);
        return RestResponse.ok();
    }

    /**
     * 查询
     */
    @GetMapping("/list")
    public RestResponse list() {
        return RestResponse.ok(commonTermsService.list(new QueryWrapper<CommonTerms>().lambda()
                .eq(CommonTerms::getUserId, SecurityUtils.getUser().getId())));
    }
}