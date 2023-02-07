package cn.cuptec.faros.task;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.FollowUpPlanNotice;
import cn.cuptec.faros.service.FollowUpPlanNoticeService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 随访计划定时任务 每天凌晨1点执行
 */
@Component
public class FollowUpPlanTask {
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;//随访计划通知模版
    @Autowired
    public RedisTemplate redisTemplate;
    /**
     * 每天凌晨1点 执行  推送患者随访计划
     */
     @Scheduled(cron = "0 0 1 * * ?")
    public void cancelOrder() {
        //生成今天的推送任务 存入redis过期推送
         LocalDate now = LocalDate.now();
         DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         String format = df.format(now);
         String startTime = format + " 00:00:00";
         String endTime = format + " 24:00:00";
         List<FollowUpPlanNotice> list = followUpPlanNoticeService.list(new QueryWrapper<FollowUpPlanNotice>().lambda()
                 .ge(FollowUpPlanNotice::getNoticeTime, startTime)
                 .le(FollowUpPlanNotice::getNoticeTime, endTime));
         if(!CollectionUtils.isEmpty(list)){
            for(FollowUpPlanNotice followUpPlanNotice:list){
                LocalDateTime noticeTime = followUpPlanNotice.getNoticeTime();
                LocalDateTime thisNow = LocalDateTime.now();
                java. time.Duration duration = java.time.Duration.between(thisNow,  noticeTime );
                long minutes = duration.toMinutes();//分钟
                //加入redis
                String keyRedis = String.valueOf(StrUtil.format("{}{}", "followUpPlanNotice:", followUpPlanNotice.getId()));
                redisTemplate.opsForValue().set(keyRedis, followUpPlanNotice.getId(), minutes, TimeUnit.MINUTES);//设置过期时间


            }

         }

    }

    public static void main(String[] args) {
        LocalDateTime noticeTime = LocalDateTime.now();
        Map<LocalDateTime,String> map=new HashMap<>();
        map.put(noticeTime,"ll");
        System.out.println(map.get(noticeTime));
    }
}
