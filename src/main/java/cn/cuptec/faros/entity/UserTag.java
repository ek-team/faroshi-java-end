package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/24 13:42
 */
@Data
public class UserTag {
    private List<String> openid_list;
    private int tagid;

}
