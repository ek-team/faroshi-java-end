package cn.cuptec.faros.bo;


import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
public class DoctorInfoBO implements Serializable{
    private static final long serialVersionUID = 5354183441192733802L;

    private User userInfo;
    private HospitalInfo hospitalInfo;
}
