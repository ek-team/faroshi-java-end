package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.User;
import lombok.Data;

@Data
public class DoctorQrCodeDto {
    private String name;
    private String qrCode;
    private String hospitalName;
}
