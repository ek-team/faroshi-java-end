package cn.cuptec.faros.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class ImMsgCallbackDTO implements Serializable {
    private static final long serialVersionUID = 1L;




    private String CallbackCommand;

    private Integer From_Account;

    private Integer To_Account;

    private Long MsgSeq;


    private Long MsgRandom;
    private Long MsgTime;



    private String MsgKey;

    private Integer OnlineOnlyFlag;




    private List<MsgBody> MsgBody;



    private String CloudCustomData;

    @Data
   static public class MsgBody implements Serializable {
        private static final long serialVersionUID = 1L;
        private String MsgType;
        private MsgContent MsgContent;

        private Integer msgId;
        private Integer roomId;





    }

    @Data
    static public class MsgContent implements Serializable {
        private static final long serialVersionUID = 1L;
        private String Text;
        private String Url;
        private Integer Size;
        private Integer Second;
        private Integer Download_Flag;

        private String UUID;
        private Long ImageFormat;
        private List<ImageInfoArray> ImageInfoArray;

    }


    @Data
    static public class ImageInfoArray implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long Type;
        private Long Size;
        private Long Width;
        private Long Height;
        private String URL;

    }
}
