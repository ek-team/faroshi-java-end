package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Notice;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.service.NoticeService;
import cn.cuptec.faros.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知记录 支付 医嘱消息
 */
@RestController
@RequestMapping("/noticeRecord")
public class NoticeController extends AbstractBaseController<NoticeService, Notice> {

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {

        return RestResponse.ok(service.getById(id));

    }

    @Override
    protected Class<Notice> getEntityClass() {
        return Notice.class;
    }
}
