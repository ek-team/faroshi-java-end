package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class QueryCompanyResult {
    private String Status;
    private List<CompanyResult> Result;
}
