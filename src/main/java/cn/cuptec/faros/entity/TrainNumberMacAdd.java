package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TrainNumberMacAdd {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer trainNumber;//训练次数
    private String macAdd;

}
