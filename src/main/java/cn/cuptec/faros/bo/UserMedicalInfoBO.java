package cn.cuptec.faros.bo;

import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserMedicalInfoBO implements Serializable {
    private static final long serialVersionUID = -7484162361858467638L;

    private User userInfo;
    private TbTrainUser  MedicalInfo;
}
