package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.TbTrainUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PlanUserMapper extends BaseMapper<TbTrainUser> {

    @Select("SELECT ttu.* from user_doctor_relation udr " +
            "LEFT JOIN `user` u ON udr.user_id = u.id " +
            "RIGHT JOIN tb_train_user ttu on u.id = ttu.xt_user_id " +
            "where udr.doctor_id = #{doctorId}")
    IPage<TbTrainUser> getPageUserByDoctorId(IPage<TbTrainUser> page, @Param("doctorId") Integer doctorId);


    @Insert("<script>"
            + "INSERT INTO faros.tb_train_user ( "
            + "`user_id`, `key_id`, `name`, `case_history_no`,`xt_user_id`, `age`, `date`, `sex`, `diagnosis`, `photo`, `doctor`" +
            ", `hospital_name`, `hospital_address`, `address`, `tele_phone`, `link_man`, `ping_yin`, `create_date`, `update_date`" +
            ", `remark`, `weight`, `evaluate_weight`, `account`,`id_card`, `password`, `str`"
            + ") " +
            "VALUES " +
            " <foreach collection='userBeanList'  item='item' separator=',' open='' close=''> "
            + "( #{item.userId}"
            + ", #{item.keyId}"
            + ", #{item.name}"
            + ", #{item.caseHistoryNo}"
            + ", #{item.xtUserId}"
            + ", #{item.age}"
            + ", #{item.date}"
            + ", #{item.sex}"
            + ", #{item.diagnosis}"
            + ", #{item.photo}"
            + ", #{item.doctor}"
            + ", #{item.hospitalName}"
            + ", #{item.hospitalAddress}"
            + ", #{item.address}"
            + ", #{item.telePhone}"
            + ", #{item.linkMan}"
            + ", #{item.pingYin}"
            + ", #{item.createDate}"
            + ", #{item.updateDate}"
            + ", #{item.remark}"
            + ", #{item.weight}"
            + ", #{item.evaluateWeight}"
            + ", #{item.account}"
            + ", #{item.idCard}"
            + ", #{item.password}"
            + ", #{item.str})"
            + "</foreach>"
            + " ON DUPLICATE KEY "
            + "<trim prefix=\"UPDATE\" prefixOverrides=\",\">  "
            + ", name = ifnull(VALUES(name),name)"
            + ", case_history_no = ifnull(VALUES(case_history_no),case_history_no)"
            + ", age = ifnull(VALUES(age),age)"
            + ", date = ifnull(VALUES(date),date)"
            + ", sex = ifnull(VALUES(sex),sex)"
            + ", diagnosis = ifnull(VALUES(diagnosis),diagnosis)"
            + ", photo = ifnull(VALUES(photo),photo)"
            + ", doctor = ifnull(VALUES(doctor),doctor)"
            + ", hospital_name = ifnull(VALUES(hospital_name),hospital_name)"
            + ", hospital_address = ifnull(VALUES(hospital_address),hospital_address)"
            + ", address = ifnull(VALUES(address),address)"
            + ", tele_phone = ifnull(VALUES(tele_phone),tele_phone)"
            + ", link_man = ifnull(VALUES(link_man),link_man)"
            + ", ping_yin = ifnull(VALUES(ping_yin),ping_yin)"
            + ", update_date = now()"
            + ", remark = ifnull(VALUES(remark),remark)"
            + ", weight = ifnull(VALUES(weight),weight)"
            + ", evaluate_weight = ifnull(VALUES(evaluate_weight),evaluate_weight)"
            + ", account = ifnull(VALUES(account),account)"
            + ", id_card = ifnull(VALUES(id_card),id_card)"
            + ", password = ifnull(VALUES(password),password)"
            + ", str = ifnull(VALUES(str),str)"
            +"</trim>"
            + "</script>")
    int batchSaveOrUpdate(@Param("userBeanList") List<TbTrainUser> userBeanList);

}
