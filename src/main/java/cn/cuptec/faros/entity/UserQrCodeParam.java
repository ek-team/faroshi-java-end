package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class UserQrCodeParam {
    private String userId;
    private List<String> qrCodeIds;
}
