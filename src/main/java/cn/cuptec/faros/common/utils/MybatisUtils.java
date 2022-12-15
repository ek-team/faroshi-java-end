package cn.cuptec.faros.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MybatisUtils {

    public static <T> Page<T> transforPage(Page oldPage){
        if (oldPage != null){
            Page<T> page = new Page<>();
            page.setSize(oldPage.getSize());
            page.setTotal(oldPage.getTotal());
            page.setCurrent(oldPage.getCurrent());
            page.setAsc(oldPage.ascs());
            page.setPages(oldPage.getPages());
            return page;
        }
        return new Page<>();
    }

}
