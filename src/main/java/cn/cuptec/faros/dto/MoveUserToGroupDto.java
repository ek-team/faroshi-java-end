package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;

@Data
public class MoveUserToGroupDto {
    private Integer userId;
    private List<Integer> groupIds;
}
