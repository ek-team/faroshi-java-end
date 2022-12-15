package cn.cuptec.faros.vo;

import lombok.Data;

import java.util.List;

@Data
public class FlitItems {

    private Integer flittingOrderId;

    private List<Integer> productStockIds;

}
