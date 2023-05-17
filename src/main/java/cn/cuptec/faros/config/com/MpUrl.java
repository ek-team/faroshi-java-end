package cn.cuptec.faros.config.com;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wx.mpurl")
@Data
public class MpUrl {
    private String sendTopic;
    private String sendLiveQrCodeNotice;
    private String sendSubNotice;
    private String paySuccessNotice;
    private String paySuccessNoticeSalesman;
    private String shipNotice;
    private String refundNotice;
    private String faPiaoNotice;
    private String sendFollowUpPlanNotice;
    private String sendDoctorUrlTip;
    private String sendDoctorTip;
    private String patientAddDoctor;
}
