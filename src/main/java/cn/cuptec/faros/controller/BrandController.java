package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Brand;
import cn.cuptec.faros.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/brand")
public class BrandController extends AbstractBaseController<BrandService, Brand> {

    /**
     * 品牌列表，分页管理
     * @param
     * @return
     */
    @GetMapping("/page")
    public RestResponse pageList(){
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<Brand> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
//        return RestResponse.ok(service.queryBrandList());
    }

    @GetMapping("/listAll")
    public RestResponse listAll(){
        return RestResponse.ok(service.list());
    }

    /**
     * 品牌的增加
     * @param brand
     * @return
     */

    @PostMapping("/add")
    public RestResponse<Brand> addBrand(@RequestBody Brand brand) {
        service.save(brand);
        return RestResponse.ok();
    }

    /**
     * 品牌的删除  del_flag == 0 表示删除
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    public RestResponse brandDel(@PathVariable String id) {
        return RestResponse.ok(service.removeById(id) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    /**
     * 品牌的修改
     * @param brand
     * @return
     */
    @PutMapping("/update")
    public RestResponse updateBrand(@Valid @RequestBody Brand brand) {
        UpdateWrapper<Brand> updateWrapper = new UpdateWrapper<Brand>();
        updateWrapper.eq("id", brand.getId());
        boolean issuccess = service.update(brand, updateWrapper);
        return issuccess ? RestResponse.ok(DATA_UPDATE_SUCCESS, null) : RestResponse.failed(DATA_UPDATE_FAILED, null);
    }

    @Override
    protected Class<Brand> getEntityClass() {
        return Brand.class;
    }
}

