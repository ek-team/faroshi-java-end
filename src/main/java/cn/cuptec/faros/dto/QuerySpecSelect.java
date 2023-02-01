package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuerySpecSelect {
    private List<Integer> specDescId;
    private Integer servicePackId;
}
