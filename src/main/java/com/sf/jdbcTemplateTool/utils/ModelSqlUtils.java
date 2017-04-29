package com.sf.jdbcTemplateTool.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sf.jdbcTemplateTool.annotation.Ignore;
import com.sf.jdbcTemplateTool.annotation.Operator;
import com.sf.jdbcTemplateTool.exception.NoColumnAnnotationFoundException;
import com.sf.jdbcTemplateTool.exception.NoDefinedGetterException;
import com.sf.jdbcTemplateTool.exception.NoIdAnnotationFoundException;
import com.sf.jdbcTemplateTool.model.SqlParamsPairs;

public class ModelSqlUtils {

    protected final static Log logger = LogFactory.getLog(ModelSqlUtils.class);

    /**
     * 从po对象中分析出insert语句
     * 
     * @param po
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static <T> SqlParamsPairs getInsertFromObject(T po) throws Exception {

        // 用来存放insert语句
        StringBuffer insertSql = new StringBuffer();
        // 用来存放?号的语句
        StringBuffer paramsSql = new StringBuffer();

        // 用来存放参数值
        List<Object> params = new ArrayList<Object>();

        // 分析表名
        String tableName = getTableName(po.getClass());

        insertSql.append("insert into " + tableName + " (");

        // 计数器
        int count = 0;

        // 分析列
        List<Field> fieldList = getFieldList(po);
        for (Field f : fieldList) {

            if ("serialVersionUID".equals(f.getName())) {
                continue;
            }

            // 获取具体参数值
            Method getter = getGetter(po.getClass(), f);

            if (getter == null) {
                continue;
            }

            Object value = getter.invoke(po);
            if (value == null) {
                // 如果参数值是null就直接跳过（不允许覆盖为null值，规范要求更新的每个字段都要有值，没有值就是空字符串）
                continue;
            }

            Transient tranAnno = getter.getAnnotation(Transient.class);
            if (tranAnno != null) {
                // 如果有 Transient 标记直接跳过
                continue;
            }

            // 获取字段名
            String columnName = getColumnNameFromGetter(getter, f);

            if (count != 0) {
                insertSql.append(",");
            }
            insertSql.append(columnName);

            if (count != 0) {
                paramsSql.append(",");
            }
            paramsSql.append("?");

            params.add(value);
            count++;
        }

        insertSql.append(") values (");
        insertSql.append(paramsSql + ")");

        SqlParamsPairs sqlAndParams = new SqlParamsPairs(insertSql.toString(), params.toArray());
        logger.debug(sqlAndParams.toString());

        return sqlAndParams;

    }

    private static <T> List<Field> getFieldList(T po) {
        List<Field> fieldList = new ArrayList<>();
        Class<? extends Object> tempClass = po.getClass();
        while (tempClass != null && !tempClass.getName().toLowerCase().equals("java.lang.object")) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        return fieldList;
    }

    private static <T> List<Field> getFieldListWithClass(Class<T> clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class<? extends Object> tempClass = clazz;
        while (tempClass != null && !tempClass.getName().toLowerCase().equals("java.lang.object")) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        return fieldList;
    }

    /**
     * 获取属性的getter方法
     * 
     * @param clazz
     * @param f
     * @return
     */
    private static <T> Method getGetter(Class<T> clazz, Field f) {
        String getterName = "get" + CamelNameUtils.capitalize(f.getName());
        Method getter = null;
        try {
            getter = clazz.getMethod(getterName);
        } catch (Exception e) {
            logger.debug(getterName + " doesn't exist!", e);
        }
        return getter;
    }

    /**
     * 从po类获取表名
     * 
     * @param po
     * @return
     */
    private static <T> String getTableName(Class<T> clazz) {

        Table tableAnno = clazz.getAnnotation(Table.class);
        if (tableAnno != null) {
            if (tableAnno.catalog() != null && !tableAnno.catalog().trim().equals("")) {
                return tableAnno.catalog() + "." + tableAnno.name();
            }
            return tableAnno.name();
        }
        // if Table annotation is null
        String className = clazz.getName();
        return CamelNameUtils.camel2underscore(className.substring(className.lastIndexOf(".") + 1));
    }

    /**
     * 从对象中获取update语句
     * 
     * @param po
     * @return
     * @throws Exception
     */
    public static SqlParamsPairs getUpdateFromObject(Object po) throws Exception {

        // 用来存放insert语句
        StringBuffer updateSql = new StringBuffer();

        // 用来存放where语句
        StringBuffer whereSql = new StringBuffer();

        // 用来存放参数值
        List<Object> params = new ArrayList<Object>();

        // 用来存储id
        Object idValue = null;

        // 分析表名
        String tableName = getTableName(po.getClass());

        updateSql.append("update " + tableName + " set");
        // 用于寻找id字段
        Id idAnno = null;
        // 用于计数
        int count = 0;
        // 分析列
        List<Field> fieldList = getFieldList(po);
        for (Field f : fieldList) {

            // 获取具体参数值
            Method getter = getGetter(po.getClass(), f);

            if (getter == null) {
                continue;
            }

            Object value = getter.invoke(po);
            if (value == null) {
                // 如果参数值是null就直接跳过（不允许覆盖为null值，规范要求更新的每个字段都要有值，没有值就是空字符串）
                continue;
            }

            Transient tranAnno = getter.getAnnotation(Transient.class);
            if (tranAnno != null) {
                // 如果有 Transient 标记直接跳过
                continue;
            }

            // 获取字段名
            String columnName = getColumnNameFromGetter(getter, f);

            // 看看是不是主键
            idAnno = getter.getAnnotation(Id.class);
            if (idAnno != null) {
                // 如果是主键
                whereSql.append(columnName + " = ?");
                idValue = value;
                continue;
            }

            // 如果是普通列
            params.add(value);

            if (count != 0) {
                updateSql.append(",");
            }
            updateSql.append(" " + columnName + " = ?");

            count++;
        }
        
        // 全部遍历完如果找不到主键就抛异常
        if (idValue == null) {
            throw new NoIdAnnotationFoundException(po.getClass());
        }

        updateSql.append(" where ");
        updateSql.append(whereSql);
        params.add(idValue);

        SqlParamsPairs sqlAndParams = new SqlParamsPairs(updateSql.toString(), params.toArray());
        logger.debug(sqlAndParams.toString());

        return sqlAndParams;

    }

    /**
     * 从对象中获取delete语句
     * 
     * @param po
     * @return
     * @throws Exception
     */
    public static SqlParamsPairs getDeleteFromObject(Object po) throws Exception {

        // 用来存放insert语句
        StringBuffer deleteSql = new StringBuffer();

        // 用来存储id
        Object idValue = null;

        // 分析表名
        String tableName = getTableName(po.getClass());

        deleteSql.append("delete from " + tableName + " where ");

        // 用于寻找id字段
        Id idAnno = null;
        // 分析列
        List<Field> fieldList = getFieldList(po);

        for (Field f : fieldList) {

            // 找id字段
            Method getter = getGetter(po.getClass(), f);

            if (getter == null) {
                // 没有get方法直接跳过
                continue;
            }

            // 看是不是主键
            idAnno = getter.getAnnotation(Id.class);
            if (idAnno == null) {
                continue;
            }

            // 看有没有定义column
            String columnName = getColumnNameFromGetter(getter, f);

            deleteSql.append(columnName + " = ?");

            idValue = getter.invoke(po, new Object[] {});

            break;
        }

        // 全部遍历完如果找不到主键就抛异常
        if (idAnno == null) {
            throw new NoIdAnnotationFoundException(po.getClass());
        }

        SqlParamsPairs sqlAndParams = new SqlParamsPairs(deleteSql.toString(), new Object[] { idValue });
        logger.debug(sqlAndParams.toString());

        return sqlAndParams;

    }

    /**
     * 获取根据主键查对象的sql和参数
     * 
     * @param po
     * @param id
     * @return
     * @throws NoIdAnnotationFoundException
     * @throws NoColumnAnnotationFoundException
     * @throws NoDefinedGetterException
     * @throws @throws
     *             Exception
     */
    public static <T> SqlParamsPairs getGetFromObject(Class<T> clazz, Object id) throws NoIdAnnotationFoundException, NoColumnAnnotationFoundException {

        // 用来存放get语句
        StringBuffer getSql = new StringBuffer();

        // 分析表名
        String tableName = getTableName(clazz);

        getSql.append("select * from " + tableName + " where ");

        // 用于寻找id字段
        Id idAnno = null;
        // 分析列
        List<Field> fieldList = getFieldListWithClass(clazz);

        for (Field f : fieldList) {

            // 找id字段
            Method getter = getGetter(clazz, f);

            if (getter == null) {
                // 没有get方法直接跳过
                continue;
            }

            // 看是不是主键
            idAnno = getter.getAnnotation(Id.class);
            if (idAnno == null) {
                continue;
            }

            // get column name
            String columnName = getColumnNameFromGetter(getter, f);

            getSql.append(columnName + " = ?");

            break;
        }

        // 全部遍历完如果找不到主键就抛异常
        if (idAnno == null) {
            throw new NoIdAnnotationFoundException(clazz);
        }

        SqlParamsPairs sqlAndParams = new SqlParamsPairs(getSql.toString(), new Object[] { id });
        logger.debug(sqlAndParams.toString());

        return sqlAndParams;
    }
    /**
     * 根据传入的对象自动生成查询SQL
     * @param po
     * @return SqlParamsPairs
     * @throws NoIdAnnotationFoundException
     * @throws NoColumnAnnotationFoundException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <T> SqlParamsPairs getSearchFromObject(Object po,int rows ,int offset) throws NoIdAnnotationFoundException, NoColumnAnnotationFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        // 用来存放get语句
        StringBuffer sql = new StringBuffer();
        // 用来存放参数值
        List<Object> params = new ArrayList<Object>();
        // 分析表名
        Class<? extends Object> clazz = po.getClass();
        String tableName = getTableName(clazz);

        sql.append("select * from " + tableName + " where 1=1 ");

        // 分析列
        List<Field> fieldList = getFieldList(po);

        for (Field f : fieldList) {
            // 找id字段
            Method getter = getGetter(clazz, f);
            if (getter == null) {
                // 没有get方法直接跳过
                continue;
            }

            Ignore ignore = getter.getAnnotation(Ignore.class);
            if (ignore != null) {
                //直接跳过
                continue;
            }
            Object value = getter.invoke(po);
            if (value == null) {
                // 如果参数值是null就直接跳过
                continue;
            }
            // get column name
            String columnName = getColumnNameFromGetter(getter, f);

            Operator anno = getter.getAnnotation(Operator.class);
            if (anno == null) {
                sql.append(" and " + columnName + " = ? ");
                params.add(value);
            } else {
                String annoValue = anno.value();
                String targetColumn = "".equals(anno.targetColumn())?columnName:anno.targetColumn();
                if("LIKE".equals(annoValue.trim().toUpperCase())){
                    sql.append(" and " + targetColumn+ " " + anno.value() + " ? ");
                    params.add("%"+value+"%");
                }else if("IN".equals(annoValue.trim().toUpperCase())){
                    sql.append(" and " + targetColumn+ " " + anno.value() + " ("+value+")");
                }else{
                    sql.append(" and " + targetColumn+ " " + anno.value() + " ? ");
                    params.add(value);
                }
            }
        }
        
        if(rows>0){
            sql.append(" limit " + rows);
        }
        
        if(offset>0){
            sql.append(","+offset);
        }
        
        SqlParamsPairs sqlAndParams = new SqlParamsPairs(sql.toString(), params.toArray());
        logger.debug(sqlAndParams.toString());

        return sqlAndParams;
    }

    /**
     * use getter to guess column name, if there is annotation then use
     * annotation value, if not then guess from field name
     * 
     * @param getter
     * @param clazz
     * @param f
     * @return
     * @throws NoColumnAnnotationFoundException
     */
    private static String getColumnNameFromGetter(Method getter, Field f) {
        String columnName = "";
        Column columnAnno = getter.getAnnotation(Column.class);
        if (columnAnno != null) {
            // 如果是列注解就读取name属性
            columnName = columnAnno.name();
        }

        if (columnName == null || "".equals(columnName)) {
            // 如果没有列注解就用命名方式去猜
            columnName = CamelNameUtils.camel2underscore(f.getName());
        }
        return columnName;
    }

}
