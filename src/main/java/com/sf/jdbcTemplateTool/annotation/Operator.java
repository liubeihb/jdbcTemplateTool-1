package com.sf.jdbcTemplateTool.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于自动生成SQL语句
 * 标注该注解后会生成  targetColumn+value+? 的查询条件
 * <ul>
 * <li>2017年4月20日 | 史锋 | 新增</li>
 * </ul>
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Operator {
    
    String targetColumn() default "";
    
    String value() default " = ";
}
