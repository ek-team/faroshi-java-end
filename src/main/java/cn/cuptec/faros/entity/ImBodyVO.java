package cn.cuptec.faros.entity;

import cn.cuptec.faros.dto.ImMsgCallbackDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ImBodyVO implements Serializable {
    private static final long serialVersionUID = -636235944849201243L;
    @JSONField(name = "ActionStatus")
    private String  ActionStatus;
    @JSONField(name = "ErrorInfo")
    private String  ErrorInfo;
    @JSONField(name = "ErrorCode")
    private Integer  ErrorCode;
    @JSONField(name = "MsgBody")
    private List<ImMsgCallbackDTO.MsgBody>  MsgBody;
    @JSONField(name = "CloudCustomData")
    private String  CloudCustomData;

    @JSONField(name = "QueryResult")
    private List<OnlineStatus>  QueryResult;

    @JSONField(name = "ErrorList")
    private List<OnlineStatus>  ErrorList;


    public static ImBodyVO success(){
        ImBodyVO vo = new ImBodyVO();
        vo.setActionStatus("OK");
        vo.setErrorInfo("");
        vo.setErrorCode(0);


        return vo;
    }
    public static ImBodyVO success(List<ImMsgCallbackDTO.MsgBody> msgBodyList,JSONObject cloudCustomDataJson){
        ImBodyVO vo = new ImBodyVO();
        vo.setActionStatus("OK");
        vo.setErrorInfo("");
        vo.setErrorCode(0);
//        vo.setMsgBody(msgBodyList);
        vo.setCloudCustomData(cloudCustomDataJson.toJSONString());

        return vo;
    }


    public static ImBodyVO fail(){
        ImBodyVO vo = new ImBodyVO();
        vo.setActionStatus("OK");
        vo.setErrorInfo("");
        vo.setErrorCode(1);
        return vo;
    }
    @Data
    static public class OnlineStatus implements Serializable {
        private static final long serialVersionUID = 1L;
        private String To_Account;
        private String Status;

        private Integer ErrorCode;
    }


}
