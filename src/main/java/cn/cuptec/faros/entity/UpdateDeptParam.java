package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class UpdateDeptParam {
    private Integer ServicePackId;
    private List<Integer> deptIds;
}
