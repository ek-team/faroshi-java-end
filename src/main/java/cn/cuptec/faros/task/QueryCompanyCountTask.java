package cn.cuptec.faros.task;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 每天凌晨1点执行 用户查询公司信息恢复为10次
 */
@Component
public class QueryCompanyCountTask {
    @Resource
    private UserService userService;

    /**
     * 每天凌晨1点执行 用户查询公司信息恢复为10次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void task() {
        userService.update(Wrappers.<User>lambdaUpdate()
                .set(User::getQueryCompanyCount, 10)

        );
    }
}
