package cn.cuptec.faros.entity;

import lombok.Data;

@Data
public class ContactInfo {
    private Integer contactType;// 1，寄件方信息 2，到件方信息
    private String country;
    private String address;//详细地址
    private String company;//公司名称
    private String contact;//联系人
    private String tel;//联系电话（tel和mobile字段必填其中一个）
    private String mobile;//手机（tel和mobile字段必填其中一个）
}
