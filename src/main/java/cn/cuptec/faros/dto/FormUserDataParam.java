package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.FormUserData;
import lombok.Data;

import java.util.List;

@Data
public class FormUserDataParam {
    private List<FormUserData> formManagementDatas;
    private Integer str;
}
