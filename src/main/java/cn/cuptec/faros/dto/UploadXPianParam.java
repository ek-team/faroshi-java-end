package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;
@Data
public class UploadXPianParam {
   private List<String> urls;
   private String xtUserId;
   private String createTime;
}
