package cn.cuptec.faros.entity;

import cn.cuptec.faros.groups.Update;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description: 部门和城市
 * @Author mby
 * @Date 2020/9/7 9:44
 */
@Data
public class DeptCity extends Model<DeptCity> {

    private Integer deptId;

    private Integer cityId;
}
