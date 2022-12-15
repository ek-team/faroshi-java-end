package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 用户地址管理
 */
@Data
public class Address extends Model<Address> {
    public Integer id;
    public Integer isDefault;//1-默认 0-不默认
    public Integer patientId;

    public String addresseeName;//姓名
    public String addresseePhone;//手机号
    public String address;//详细地址
    public String city;//市
    public String area;//区
    public String province;//省
}
