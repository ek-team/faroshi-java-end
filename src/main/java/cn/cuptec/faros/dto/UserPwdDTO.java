package cn.cuptec.faros.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPwdDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    private String oldPwd;

    private String newPwd;


}
