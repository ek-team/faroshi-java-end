package cn.cuptec.faros.dto;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
public class PageResult {
    private Integer total;
    private Object records;
}
