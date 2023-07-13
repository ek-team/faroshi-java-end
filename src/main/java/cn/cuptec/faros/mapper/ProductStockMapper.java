package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.ProductStock;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.security.core.parameters.P;

import java.util.List;


public interface ProductStockMapper extends BaseMapper<ProductStock> {

    @Update("update product_stock a set a.status = 2 where a.id =#{ids} ")
    int updateProductStockStatus(@Param("ids")String ids);

    @Update("UPDATE product_stock SET live_qr_code_id = null WHERE live_qr_code_id = #{liveQrCodeId}")
    void unbindQrcode(String liveQrCodeId);

    @Select(" Select * from product_stock where live_qr_code_id = #{liveQrCodeId}")
    ProductStock  getproductStockByCodeId(String liveQrCodeId);

    /**
     * 根据用户id查询产品列表
     * @param id 用户id
     * @return
     */
    @Select("SELECT product_stock.*, product.product_name, product.product_pic from product_stock LEFT JOIN product ON product_stock.product_id = product.id WHERE product_stock.user_id = #{id}")
    List<ProductStock> listCustomerProduct(Integer id);

    @Select("SELECT product_stock.*, product.product_name, product.product_pic from product_stock LEFT JOIN product ON product_stock.product_id = product.id WHERE product_stock.id = #{id}")
    ProductStock getDetailById(int id);

    @Update("UPDATE product_stock SET user_id = null WHERE id = #{id}")
    void clearOrderUid(Integer id);

    /**
     * 根据数据权限分页查询设备列表
     * @param page
     * @param wrapper
     * @param dataScope
     * @return
     */
    @Select("SELECT product_stock.*, locator.locator_name, dept.name as deptName FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN dept ON product_stock.dept_id = dept.id " +
            "${ew.customSqlSegment} ORDER BY create_date DESC")
    IPage<ProductStock> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select(" <script> SELECT  product_stock.*,hospital_info.name as hospitalName, locator.locator_name, dept.name as deptName, user.nickname as salesmanName FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN dept ON product_stock.dept_id = dept.id " +
            "LEFT JOIN user ON product_stock.salesman_id = user.id " +
            "LEFT JOIN hospital_info ON product_stock.hospital_id = hospital_info.id " +
            "${ew.customSqlSegment}" +
            "<if test=\"nickname != null\">"
            + "and  user.nickname LIKE CONCAT('%',#{nickname},'%') "
            + "</if>"+
            "<if test=\"activationDate != null and activationDate != '' \">"
            + "and  product_stock.activation_date  &lt;= #{activationDate}  "
            + "</if>"+
            "<if test=\"productSn != null and productSn != '' \">"
            + "and  product_stock.product_sn LIKE CONCAT('%',#{productSn},'%') "
            + "</if>"+
            "<if test=\"hospitalInfo != null and hospitalInfo != ''\">"
            + "and  hospital_info.name LIKE CONCAT('%',#{hospitalInfo},'%') "
            + "</if>"+
            "<if test=\"phone != null\">"
            + "and  user.phone LIKE CONCAT('%',#{phone},'%') "
            + "</if>"+
            "<if test=\"productId != null and productId != '' \">"
            + "and  product_stock.product_id= #{productId} "
            + "</if>"+
            "<if test=\"macAdd != null\">"
            + "and  product_stock.mac_address LIKE CONCAT('%',#{macAdd},'%') "
            + "</if>"
            + " ORDER BY activation_date ${sort} </script>")
    IPage<ProductStock> pageScopedDep(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope,@Param("activationDate") String activationDate, @Param("nickname") String nickname, @Param("phone") String phone,@Param("macAdd") String macAdd,@Param("productSn") String productSn,@Param("hospitalInfo") String hospitalInfo,@Param("sort") String sort,@Param("productId") String productId);
    @Select(" <script> SELECT  product_stock.*,hospital_info.name as hospitalName, locator.locator_name, dept.name as deptName, user.nickname as salesmanName FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN dept ON product_stock.dept_id = dept.id " +
            "LEFT JOIN user ON product_stock.salesman_id = user.id " +
            "LEFT JOIN hospital_info ON product_stock.hospital_id = hospital_info.id " +
            "where 1=1"+
            "<if test=\"nickname != null\">"
            + "and  user.nickname LIKE CONCAT('%',#{nickname},'%') "
            + "</if>"+
            "<if test=\"activationDate != null and activationDate != '' \">"
            + "and  product_stock.activation_date  &lt;= #{activationDate}  "
            + "</if>"+
            "<if test=\"productSn != null and productSn != '' \">"
            + "and  product_stock.product_sn LIKE CONCAT('%',#{productSn},'%') "
            + "</if>"+
            "<if test=\"hospitalInfo != null and hospitalInfo != ''\">"
            + "and  hospital_info.name LIKE CONCAT('%',#{hospitalInfo},'%') "
            + "</if>"+
            "<if test=\"phone != null\">"
            + "and  user.phone LIKE CONCAT('%',#{phone},'%') "
            + "</if>"+
            "<if test=\"productId != null and productId != '' \">"
            + "and  product_stock.product_id = #{productId} "
            + "</if>"+
            "<if test=\"macAdd != null\">"
            + "and  product_stock.mac_address LIKE CONCAT('%',#{macAdd},'%') "
            + "</if>"
            + " ORDER BY activation_date ${sort} </script>")
    IPage<ProductStock> pageScopedDepAll(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, @Param("activationDate") String activationDate, @Param("nickname") String nickname, @Param("phone") String phone,@Param("macAdd") String macAdd,@Param("productSn") String productSn,@Param("hospitalInfo") String hospitalInfo,@Param("sort") String sort,@Param("productId") String productId);

    @Select("SELECT product_stock.*, locator.locator_name, product.product_name, product.product_pic, product.product_type FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN product ON product_stock.product_id = product.id " +
            "${ew.customSqlSegment} and product_stock.locator_id is not null  ORDER BY create_date DESC")
    List<ProductStock> listScoped(@Param(Constants.WRAPPER) Wrapper queryWrapper, DataScope dataScope);

    @Select("SELECT product_stock.*, locator.locator_name, product.product_name, product.product_pic, product.product_type FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN product ON product_stock.product_id = product.id " +
            "${ew.customSqlSegment} and product_stock.locator_id is not null  ORDER BY create_date DESC")
    List<ProductStock> listScoped1(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select("SELECT product_stock.*, locator.locator_name, product.product_name, product.product_pic, product.product_type FROM product_stock " +
            "LEFT JOIN locator ON product_stock.locator_id = locator.id " +
            "LEFT JOIN product ON product_stock.product_id = product.id " +
            "${ew.customSqlSegment} ORDER BY create_date DESC")
    List<ProductStock> listScopedByStatus(@Param(Constants.WRAPPER) Wrapper queryWrapper, DataScope dataScope);
}
