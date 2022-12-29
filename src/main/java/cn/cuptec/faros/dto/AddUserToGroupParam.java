package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddUserToGroupParam {
    private List<Integer> userIds;
    private Integer userGroupId;
}
