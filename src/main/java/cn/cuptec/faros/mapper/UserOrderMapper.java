package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.UserOrder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

public interface UserOrderMapper extends BaseMapper<UserOrder> {

    @Select("SELECT user_order.id,user_order.order_no," +
            "user_order.receiver_name,user_order.receiver_phone,user_order.receiver_region," +
            "user_order.receiver_detail_address,user_order.user_id," +
            "user_order.dept_id,user_order.salesman_id,user_order.product_id," +
            "user_order.sale_price,user_order.status,user_order.create_time," +
            "user_order.delivery_sn,user_order.delievey_time,user_order.product_sn," +
            "user_order.payment,user_order.locator_id,user_order.salesman_phone," +
            "user_order.id_card," +
            "user_order.na_li_order_id," +
            "user_order.use_name," +
            "user_order.delivery_number," +
            "user_order.service_pack_id," +
            "user_order.set_meal_name," +
            "user_order.city," +
            "user_order.province," +
            "user_order.doctor_id," +
            "user_order.reduce_amount," +
            "user_order.invalid," +
            "user_order.product_name," +
            "locator.locator_name as locatorName, product.product_pic, user.nickname as salesmanName, user.phone as salesmanPhone, salesman_pay_channel.rev_money_pic_url " +
            "FROM user_order " +
            "LEFT JOIN product ON user_order.product_id = product.id " +
            "LEFT JOIN locator ON user_order.locator_id = locator.id " +
            "LEFT JOIN user ON user_order.salesman_id = user.id " +
            "LEFT JOIN salesman_pay_channel ON user_order.salesman_id = salesman_pay_channel.salesman_id " +
            "WHERE user_order.id = #{id}"
    )
    UserOrder getOrderDetail(Serializable id);

    @Select("SELECT user_order.id,user_order.order_no," +
            "user_order.receiver_name,user_order.receiver_phone,user_order.receiver_region," +
            "user_order.receiver_detail_address,user_order.user_id," +
            "user_order.dept_id,user_order.salesman_id,user_order.product_id," +
            "user_order.sale_price,user_order.status,user_order.create_time," +
            "user_order.delivery_sn,user_order.delievey_time,user_order.product_sn," +
            "user_order.payment,user_order.locator_id,user_order.salesman_phone," +
            "user_order.id_card," +
            "user_order.na_li_order_id," +
            "user_order.use_name," +
            "user_order.delivery_number," +
            "user_order.service_pack_id," +
            "user_order.set_meal_name," +
            "user_order.city," +
            "user_order.province," +
            "user_order.doctor_id," +
            "user_order.reduce_amount," +
            "user_order.invalid," +
            "FROM user_order " +
            " ${ew.customSqlSegment} " +
            "ORDER BY user_order.create_time DESC")
    IPage<UserOrder> pageMyOrder(IPage page, @Param(Constants.WRAPPER)Wrapper queryWrapper);

    @Select("SELECT user_order.id,user_order.order_no," +
            "user_order.receiver_name,user_order.receiver_phone,user_order.receiver_region," +
            "user_order.receiver_detail_address,user_order.user_id," +
            "user_order.dept_id,user_order.salesman_id,user_order.product_id," +
            "user_order.sale_price,user_order.status,user_order.create_time," +
            "user_order.delivery_sn,user_order.delievey_time,user_order.product_sn," +
            "user_order.payment,user_order.locator_id,user_order.salesman_phone," +
            "user_order.id_card," +
            "user_order.na_li_order_id," +
            "user_order.use_name," +
            "user_order.delivery_number," +
            "user_order.service_pack_id," +
            "user_order.set_meal_name," +
            "user_order.city," +
            "user_order.province," +
            "user_order.doctor_id," +
            "user_order.reduce_amount," +
            "user_order.invalid," +
            "FROM user_order " +

            "LEFT JOIN dept ON user_order.dept_id = dept.id " +
            "${ew.customSqlSegment} ORDER BY user_order.create_time DESC")
    IPage<UserOrder> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT user_order.* from user_order " +
            "${ew.customSqlSegment} ORDER BY user_order.create_time DESC")
    List<UserOrder> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);
}
