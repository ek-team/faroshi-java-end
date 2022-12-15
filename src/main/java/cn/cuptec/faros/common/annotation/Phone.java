package cn.cuptec.faros.common.annotation;

import cn.cuptec.faros.common.annotation.impl.PhoneValidated;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PhoneValidated.class)
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
public @interface Phone {
    String message() default "请填写正确的手机号";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}