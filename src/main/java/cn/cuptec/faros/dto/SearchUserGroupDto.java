package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserGroup;
import lombok.Data;

import java.util.List;

@Data
public class SearchUserGroupDto {
    private List<UserGroup> userGroupList;
    private List<User> userList;
}
