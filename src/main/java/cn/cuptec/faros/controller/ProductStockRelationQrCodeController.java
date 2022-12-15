package cn.cuptec.faros.controller;

import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.entity.ProductStockRelationQrCode;
import cn.cuptec.faros.service.ProductStockRelationQrCodeService;
import cn.cuptec.faros.service.ProductStockService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productStockRelationQrCode")
public class ProductStockRelationQrCodeController extends AbstractBaseController<ProductStockRelationQrCodeService, ProductStockRelationQrCode> {
    @Override
    protected Class<ProductStockRelationQrCode> getEntityClass() {
        return ProductStockRelationQrCode.class;
    }
}
