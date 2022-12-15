package cn.cuptec.faros.common.annotation;

import cn.cuptec.faros.common.enums.QueryLogical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creater: Miao
 * CreateTime: 2019/4/11 10:15
 * Description: 用于标识是否可根据这个实体的此字段进行数据查询
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Queryable {

    /**
     * 查询的列名
     * @return
     */
    String columnName() default "";

    /**
     * 查询条件逻辑
     * @return
     */
    QueryLogical queryLogical();

}
