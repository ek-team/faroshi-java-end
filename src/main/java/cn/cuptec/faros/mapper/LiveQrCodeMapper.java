package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.LiveQrCode;
import cn.cuptec.faros.entity.ProductStock;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LiveQrCodeMapper extends BaseMapper<LiveQrCode> {
    /**
     * 根据数据权限分页查询设备列表
     *
     * @param page
     * @param wrapper
     * @param dataScope
     * @return
     */
    @Select("SELECT live_qr_code.product_id,live_qr_code.url_name,live_qr_code.service_pack_id,live_qr_code.hospital_name,live_qr_code.url,live_qr_code.id,live_qr_code.type,live_qr_code.dept_id,live_qr_code.name,live_qr_code.icon FROM live_qr_code " +
            "LEFT JOIN dept ON live_qr_code.dept_id = dept.id " +
            "${ew.customSqlSegment}")
    IPage<LiveQrCode> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT live_qr_code.*,dept.id as deptId  FROM live_qr_code " +
            "LEFT JOIN dept ON live_qr_code.dept_id = dept.id " +
            "${ew.customSqlSegment}")
    List<LiveQrCode> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT live_qr_code.id, product_stock.product_sn,product_stock.product_id,product_stock.mac_address FROM live_qr_code LEFT JOIN product_stock ON live_qr_code.id = product_stock.live_qr_code_id LEFT JOIN product on product.id = product_stock.product_id WHERE live_qr_code.type = 1")
    IPage<LiveQrCode> pageQrcode(IPage<LiveQrCode> page);

    @Select("<script> SELECT live_qr_code.id,live_qr_code.type," +
            "hospital_info.name as hospitalName, locator.locator_name,  user.nickname as salesmanName," +
            "product_stock.activation_date , product_stock.contract_date ," +
            "product_stock.product_production_date as productProductionDate, " +
            "product_stock.mac_address ,product_stock.salesman_id , " +
            "product_stock.nani_qr_code_url ,product_stock.icc_id , " +
            "product_stock.locator_id ,product_stock.status ,product_stock.hospital_id , " +
            "product_stock.live_qr_code_id as liveQrCodeId ," +
            "product_stock.product_sn,product_stock.product_id,product_stock.id as productStockId," +
            "product_stock.mac_address FROM live_qr_code LEFT JOIN product_stock ON live_qr_code.id = product_stock.live_qr_code_id " +
            "LEFT JOIN product on product.id = product_stock.product_id " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN hospital_info ON product_stock.hospital_id = hospital_info.id " +
            "LEFT JOIN user ON product_stock.salesman_id = user.id " +
            "WHERE 1= 1"
            + "<if test=\"productSn != null or mac != null \">"
            + "<trim prefix=\" and (\"   suffix=\" ) \" prefixOverrides= \"or\">"
            + "<if test=\"productSn != null\">"
            + "product_stock.product_sn LIKE CONCAT('%',#{productSn},'%') "
            + "</if>"
            + "<if test=\"mac != null\">"
            + "or  product_stock.mac_address LIKE CONCAT('%',#{mac},'%') "
            + "</if>"
            + "</trim>"
            + "</if>"
            + " </script>")
    IPage<LiveQrCode> pageQrcodeSearch(IPage<LiveQrCode> page, @Param("productSn") String productSn, @Param("mac") String mac);

    @Select("<script> SELECT live_qr_code.id,product_stock.product_production_date as productProductionDate , product_stock.product_sn,product_stock.product_id,product_stock.mac_address FROM live_qr_code LEFT JOIN product_stock ON live_qr_code.id = product_stock.live_qr_code_id LEFT JOIN product on product.id = product_stock.product_id WHERE live_qr_code.type = 5"
            + "<if test=\"productSn != null or mac != null \">"
            + "<trim prefix=\" and (\"   suffix=\" ) \" prefixOverrides= \"or\">"
            + "<if test=\"productSn != null\">"
            + "product_stock.product_sn LIKE CONCAT('%',#{productSn},'%') "
            + "</if>"
            + "<if test=\"mac != null\">"
            + "or  product_stock.mac_address LIKE CONCAT('%',#{mac},'%') "
            + "</if>"
            + "</trim>"
            + "</if>"
            + " </script>")
    IPage<LiveQrCode> pageNaliQrcodeSearch(IPage<LiveQrCode> page, @Param("productSn") String productSn, @Param("mac") String mac);

    @Select("<script> SELECT live_qr_code.id,product_stock.product_production_date as productProductionDate , product_stock.product_sn,product_stock.product_id,product_stock.mac_address FROM live_qr_code LEFT JOIN product_stock ON live_qr_code.id = product_stock.live_qr_code_id LEFT JOIN product on product.id = product_stock.product_id WHERE live_qr_code.type = 7"
            + "<if test=\"productSn != null or mac != null \">"
            + "<trim prefix=\" and (\"   suffix=\" ) \" prefixOverrides= \"or\">"
            + "<if test=\"productSn != null\">"
            + "product_stock.product_sn LIKE CONCAT('%',#{productSn},'%') "
            + "</if>"
            + "<if test=\"mac != null\">"
            + "or  product_stock.mac_address LIKE CONCAT('%',#{mac},'%') "
            + "</if>"
            + "</trim>"
            + "</if>"
            + " </script>")
    IPage<LiveQrCode> pageHxdProductQrcode(IPage<LiveQrCode> page, @Param("productSn") String productSn, @Param("mac") String mac);



}
