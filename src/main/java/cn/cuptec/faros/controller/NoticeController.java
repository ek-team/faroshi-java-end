package cn.cuptec.faros.controller;

import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Notice;
import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.service.NoticeService;
import cn.cuptec.faros.service.ProductService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 通知记录 支付 医嘱消息
 */
@RestController
@RequestMapping("/noticeRecord")
public class NoticeController extends AbstractBaseController<NoticeService, Notice> {

    @Override
    protected Class<Notice> getEntityClass() {
        return Notice.class;
    }
}
