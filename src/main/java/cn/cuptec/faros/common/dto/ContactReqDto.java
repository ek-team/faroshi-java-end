package cn.cuptec.faros.common.dto;

import lombok.Data;

@Data
public class ContactReqDto {

    private Integer productId;

    private Integer salesmanId;

    private String username;

    private String userPhone;

}
