package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dict;
import cn.cuptec.faros.entity.NNAccount;
import cn.cuptec.faros.service.DictService;
import cn.cuptec.faros.service.NNAccountService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 诺诺商户的管理
 */
@RestController
@RequestMapping("/nNAccount")
@AllArgsConstructor
public class NNAccountController extends AbstractBaseController<NNAccountService, NNAccount> {
    private final NNAccountService nnAccountService;

    @ApiOperation(value = "添加商户")
    @PostMapping("addAccount")
    public RestResponse<Boolean> save(@RequestBody NNAccount nnAccount) {
        return RestResponse.ok(nnAccountService.save(nnAccount));
    }

    @GetMapping("/page")
    public RestResponse<IPage> page() {
        Page<NNAccount> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (page != null)
            return RestResponse.ok(nnAccountService.page(page, queryWrapper));
        else
            return RestResponse.ok();
    }

    @Override
    protected Class<NNAccount> getEntityClass() {
        return NNAccount.class;
    }
}
