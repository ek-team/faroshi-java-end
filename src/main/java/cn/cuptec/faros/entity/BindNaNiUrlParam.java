package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class BindNaNiUrlParam {
    private List<String> qrCodeIds;
    private Integer productStockId;
}
