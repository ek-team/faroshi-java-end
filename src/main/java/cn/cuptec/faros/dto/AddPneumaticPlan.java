package cn.cuptec.faros.dto;

import cn.cuptec.faros.entity.PneumaticPlan;
import lombok.Data;

import java.util.List;

@Data
public class AddPneumaticPlan {
    private List<String> deleteDate;
    private List<PneumaticPlan> datas;
}
