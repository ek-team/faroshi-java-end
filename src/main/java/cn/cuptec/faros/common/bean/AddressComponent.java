package cn.cuptec.faros.common.bean;

import lombok.Data;

/**
 * 地址解析类
 */
@Data
public class AddressComponent {

    /**
     * 国家
     */
    private String nation;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 街道
     */
    private String street;

    /**
     * 街道号码
     */
    private String street_number;

}
