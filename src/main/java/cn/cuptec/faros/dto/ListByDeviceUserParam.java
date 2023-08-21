package cn.cuptec.faros.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListByDeviceUserParam {
    private List<String> deviceUserIds;
}