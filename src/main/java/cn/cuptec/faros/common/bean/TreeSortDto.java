package cn.cuptec.faros.common.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@ApiModel(value = "树形结构排序")
@Data
public class TreeSortDto {

    @ApiModelProperty(value = "父级ID")
    @NotNull(message = "父级节点不能为空")
    private Integer parentId;

    @ApiModelProperty(value = "需要排序的节点id集合，系统根据传入的id的顺序进行排序")
    @Size(min = 1, message = "需要排序的节点不能小于1")
    private List<Integer> nodeIds;

}
