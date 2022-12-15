package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class ListByUidPlanResult {
    private List<TbPlan> data;
    private List<TbPlan> originalData;
    private Integer code;
}
