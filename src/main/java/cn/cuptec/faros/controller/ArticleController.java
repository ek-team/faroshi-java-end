package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Article;
import cn.cuptec.faros.entity.BluetoothShoesConfig;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.ArticleService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/article")
public class ArticleController extends AbstractBaseController<ArticleService, Article> {
    @Resource
    private UserService userService;

    @SysLog("添加")
    @PostMapping("/save")
    public RestResponse<Boolean> save(@Valid @RequestBody Article article) {
        User user = userService.getById(SecurityUtils.getUser().getId());
        article.setCreateUserId(user.getId());
        article.setDeptId(user.getDeptId());
        return RestResponse.ok(service.save(article));
    }

    @SysLog("编辑")
    @PutMapping("/update")
    public RestResponse update(@Valid @RequestBody Article article) {
        return RestResponse.ok(service.updateById(article));
    }

    @SysLog("删除")
    @DeleteMapping("/removeById")
    public RestResponse<Boolean> removeById(@RequestParam("id") Integer id) {
        return RestResponse.ok(service.removeById(id));
    }

    @GetMapping("/list")
    public RestResponse<List<Article>> list() {
        Class<Article> entityClass = getEntityClass();
        QueryWrapper queryWrapper = getQueryWrapper(entityClass);
        queryWrapper.select().orderByDesc("article.sort");
        return RestResponse.ok(service.list(queryWrapper));
    }

    //type 1:医院版 2：家庭版
    @GetMapping("/getShowContent")
    public RestResponse<Article> getShowContent(@RequestParam(value = "type", required = false) Integer type) {
        if (type == null) {
            type = 1;
        }
        List<Article> list = service.list(new QueryWrapper<Article>().lambda().eq(Article::getShowContent, 1).eq(Article::getCategory, type));
        if (CollectionUtils.isEmpty(list)) {
            RestResponse.ok();
        }

        return RestResponse.ok(list.get(0));
    }

    @Override
    protected Class<Article> getEntityClass() {
        return Article.class;
    }
}
