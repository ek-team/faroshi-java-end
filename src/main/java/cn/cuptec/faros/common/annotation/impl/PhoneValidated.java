package cn.cuptec.faros.common.annotation.impl;

import cn.cuptec.faros.common.annotation.Phone;
import cn.cuptec.faros.common.utils.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Creater: Miao
 * CreateTime: 2019/6/11 18:44
 * Description:
 */
public class PhoneValidated implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotEmpty(s)){
            return s.matches("1[3|4|5|6|7|8|9][0-9]\\d{8}");
        }
        return false;
    }
}
