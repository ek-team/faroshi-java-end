package cn.cuptec.faros.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeDoctorDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    private String remarks;

    private String realName;

    private String phone;

    private int hospitalId;

}
