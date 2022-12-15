//package cn.cuptec.faros.util;
//
//import cn.cuptec.faros.common.RestResponse;
//import cn.cuptec.faros.common.constrants.CommonConstants;
//import cn.cuptec.faros.entity.SysJob;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import javax.annotation.Resource;
//
///**
// * 定时任务rest反射实现
// *
// */
//@Slf4j
//@Component("restTaskInvok")
//@AllArgsConstructor
//public class RestTaskInvok implements ITaskInvok {
//    @Resource
//    private RestTemplate restTemplate;
//
//    @Override
//    public void invokMethod(SysJob sysJob) throws TaskException {
//        RestResponse restResponse = restTemplate.getForObject(sysJob.getExecutePath(), RestResponse.class);
//        if (CommonConstants.FAIL == restResponse.getCode()) {
//            log.error("定时任务restTaskInvok异常,执行任务：{}", sysJob.getExecutePath());
//            throw new TaskException("定时任务restTaskInvok业务执行失败,任务：" + sysJob.getExecutePath());
//        }
//    }
//}