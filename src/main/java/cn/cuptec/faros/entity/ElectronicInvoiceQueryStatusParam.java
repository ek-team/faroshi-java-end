package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class ElectronicInvoiceQueryStatusParam {
    private String isOfferInvoiceDetail;
    private List<String> orderNos;

}
