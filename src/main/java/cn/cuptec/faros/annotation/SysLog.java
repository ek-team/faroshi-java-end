package cn.cuptec.faros.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {

	/**
	 * 日志类型
	 */
	String type() default  "NOT_SET";

	/**
	 * 描述
	 *
	 * @return {String}
	 */
	String value();
}
