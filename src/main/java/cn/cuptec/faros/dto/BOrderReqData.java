package cn.cuptec.faros.dto;

import com.kuaidi100.sdk.request.BOrderReq;
import lombok.Data;

@Data
public class BOrderReqData extends BOrderReq {
        private String returnType;
        private String pickupStartTime;
        private String pickupEndTime;
}
