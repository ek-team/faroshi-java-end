package cn.cuptec.faros.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 新添加专家的事件
 */
@Getter
@AllArgsConstructor
public class NewDoctorEvent {

    private Integer uid;

    private Integer hospitalId;

}
