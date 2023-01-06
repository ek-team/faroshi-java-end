package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;

@Data
public class BindDiseasesParam {
    private Integer teamId;
    private List<Integer> diseasesIds;
}
