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

    @Select("SELECT user_order.id,user_order.doctor_team_id,user_order.delivery_company_code,user_order.order_no," +
            "address.addressee_name as receiverName,address.addressee_phone as receiverPhone,address.city,address.area,address.province," +
            "address.address as receiverDetailAddress,user_order.user_id," +
            "user_order.dept_id,user_order.salesman_id," +
            "user_order.status,user_order.create_time," +
            "user_order.delivery_sn,user_order.delievey_time," +
            "user_order.payment," +
            "user_order.delivery_number," +
            "user_order.service_pack_id," +
            "user_order.doctor_id" +
            " FROM user_order " +
            "LEFT JOIN address ON user_order.address_id = address.id " +
            "WHERE user_order.id = #{id}"
    )
    UserOrder getOrderDetail(Serializable id);

    @Select("SELECT user_order.bill_type,user_order.sale_spec_service_end_time,user_order.user_service_package_info_id,user_order.id,user_order.bill_image,user_order.bill_id,user_order.product_pic,user_order.receiver_detail_address,user_order.order_no,user_order.order_type, " +
            "user_order.user_id," +
            "user_order.dept_id," +
            "user_order.sale_price,user_order.status,user_order.create_time," +
            "user_order.payment,user_order.sale_spec_id,user_order.product_spec," +
            "user_order.delivery_number," +
            "user_order.service_pack_id," +
            "user_order.doctor_id" +
            " FROM user_order " +
            "LEFT JOIN service_pack ON user_order.service_pack_id = service_pack.id " +
            " ${ew.customSqlSegment} " +
            "ORDER BY user_order.create_time DESC")
    IPage<UserOrder> pageMyOrder(IPage page, @Param(Constants.WRAPPER)Wrapper queryWrapper);

    @Select("SELECT user_order.bill_type,user_order.settlement_amount,user_order.review_refund_order_id,user_order.pay_type,user_order.test,user_order.sale_spec_recovery_price,user_order.sale_spec_service_end_time,user_order.refund_review_time,user_order.refund_initiation_time,user_order.acceptance_time,user_order.recycle_time,user_order.move_time,user_order.logistics_delivery_time,user_order.product_sn3,user_order.product_sn2,user_order.product_sn1,user_order.actual_retrieve_amount,user_order.use_day,user_order.id,user_order.bill_id,user_order.product_pic,user_order.delivery_time,user_order.pay_time,hospital_info.name as  hospitalName,user_order.bill_image,user_order.delivery_date,user_order.order_no,doctor_team.name as doctorTeamName, " +
            "user_order.dept_id,user_order.receiver_phone,user_order.receiver_name,user_order.receiver_detail_address," +
            "user_order.status,user_order.create_time,user_order.remark," +
            "user_order.payment,user_order.sale_spec_id,user_order.product_spec," +
            "user_order.service_pack_id,user_order.user_id,patient_user.name as patientUserName,patient_user.id_card as patientUserIdCard " +
            " FROM user_order " +
            "LEFT JOIN dept ON user_order.dept_id = dept.id " +
            "LEFT JOIN doctor_team ON user_order.doctor_team_id = doctor_team.id " +
            "LEFT JOIN hospital_info ON doctor_team.hospital_id = hospital_info.id " +
            "LEFT JOIN patient_user ON user_order.patient_user_id = patient_user.id " +
            "LEFT JOIN service_pack ON user_order.service_pack_id = service_pack.id " +
            "${ew.customSqlSegment} ")
    IPage<UserOrder> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT user_order.product_sn3,user_order.product_sn2,user_order.product_sn1,user_order.actual_retrieve_amount,user_order.use_day,user_order.id,user_order.bill_id,user_order.product_pic,user_order.delivery_time,user_order.pay_time,hospital_info.name as  hospitalName,user_order.bill_image,user_order.delivery_date,user_order.order_no,doctor_team.name as doctorTeamName, " +
            "user_order.dept_id,user_order.receiver_phone,user_order.receiver_name,user_order.receiver_detail_address," +
            "user_order.status,user_order.create_time,user_order.remark," +
            "user_order.payment,user_order.sale_spec_id,user_order.product_spec," +
            "user_order.service_pack_id,user_order.user_id,patient_user.name as patientUserName,patient_user.id_card as patientUserIdCard " +
            " FROM user_order " +
            "LEFT JOIN dept ON user_order.dept_id = dept.id " +
            "LEFT JOIN doctor_team ON user_order.doctor_team_id = doctor_team.id " +
            "LEFT JOIN hospital_info ON doctor_team.hospital_id = hospital_info.id " +
            "LEFT JOIN patient_user ON user_order.patient_user_id = patient_user.id " +
            "LEFT JOIN service_pack ON user_order.service_pack_id = service_pack.id " +
            "${ew.customSqlSegment} ORDER BY user_order.create_time DESC")
   List<UserOrder> scoped( @Param(Constants.WRAPPER) Wrapper wrapper);



    @Select("SELECT user_order.* from user_order " +
            "${ew.customSqlSegment} ORDER BY user_order.create_time DESC")
    List<UserOrder> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT user_order.* from user_order " +
            "${ew.customSqlSegment} ORDER BY user_order.create_time DESC")
    List<UserOrder> listScopedTime(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);
}
