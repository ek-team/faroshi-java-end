package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ReportRecord;

import cn.cuptec.faros.service.ReportRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户举报
 */
@Slf4j
@RestController
@RequestMapping("/reportRecord")
public class ReportRecordController extends AbstractBaseController<ReportRecordService, ReportRecord> {
    @PostMapping("/save")
    public RestResponse save(@RequestBody ReportRecord reportRecord) {
        if (reportRecord.getCategory() != null) {
            reportRecord.setCategory(1);

        }
        reportRecord.setUserId(SecurityUtils.getUser().getId());
        reportRecord.setCreateTime(LocalDateTime.now());
        service.save(reportRecord);
        return RestResponse.ok();

    }

    @GetMapping("/page")
    public RestResponse page() {
        Page<ReportRecord> page = getPage();
        return RestResponse.ok(service.page(page, new QueryWrapper<ReportRecord>().lambda().orderByDesc(ReportRecord::getCreateTime)));
    }

    @Override
    protected Class<ReportRecord> getEntityClass() {
        return ReportRecord.class;
    }
}
