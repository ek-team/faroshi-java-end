package cn.cuptec.faros.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FlittingOrderDTO implements Serializable {
    private static final long serialVersionUID = 8628676567603667896L;


    private Integer id;
    private Integer locatorId;
    private List<Integer> productStockIds;
}
