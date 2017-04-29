package com.sf.jdbcTemplateTool;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sf.jdbcTemplateTool.exception.NoColumnAnnotationFoundException;
import com.sf.jdbcTemplateTool.exception.NoDefinedGetterException;
import com.sf.jdbcTemplateTool.exception.NoIdAnnotationFoundException;
import com.sf.jdbcTemplateTool.impl.BatchUpdateSetter;
import com.sf.jdbcTemplateTool.model.SqlParamsPairs;
import com.sf.jdbcTemplateTool.utils.IdUtils;
import com.sf.jdbcTemplateTool.utils.ModelSqlUtils;

/**
 * JdbcTemplateTool
 * <ul>
 * <li>2017年4月17日 | 史锋 | 新增</li>
 * </ul>
 */
@Component
public class JdbcTemplateTool {

	protected final Log logger = LogFactory.getLog(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}	
	//JdbcTemplateTool use proxy instead of directly use jdbcTemplate, cause it can do some AOP function in proxy. That makes code more clear.
	private JdbcTemplateProxy _proxy;
	
	//return the singleton proxy
	private JdbcTemplateProxy getProxy(){
		if(_proxy == null){
			_proxy = new JdbcTemplateProxy();
			_proxy.setJdbcTemplate(jdbcTemplate);
		}
		return _proxy;
	}
	
	   
    /**
     * 执行SQL
     * @param sql
     * @throws Exception
     */
    public void execute(String sql, Object[] params) throws Exception {
        getProxy().update(sql, params);
    }
	
	// --------- select ------------//

	/**
	 * 获取对象列表
	 * get a list of clazz
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> list(String sql, Object[] params, Class<T> clazz) {
		
		//call jdbcTemplate to query for result
		List<T> list = null;
		if (params == null || params.length == 0) {
			list = getProxy().query(sql, new BeanPropertyRowMapper(clazz));
		} else {
			list = getProxy().query(sql, params, new BeanPropertyRowMapper(clazz));
		}
		
		//return list
		return list;
	}
	
	/**
     * 根据传入的对象自动生成查询SQL
     * update object
     * @param po
     * @throws Exception
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> List<T> list(Object po) throws Exception {
        SqlParamsPairs sqlAndParams = ModelSqlUtils.getSearchFromObject(po,0,0);
        //query for list
        List<T> list = getProxy().query(sqlAndParams.getSql(), sqlAndParams.getParams(),
                new BeanPropertyRowMapper(po.getClass()));
        if (list.size() > 0) {
            return list;
        } else {
            return null;
        }
    }
	
	   /**
     * 根据传入的对象自动生成分页查询SQL
     * update object
     * @param po
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> List<T> listWithPagination(Object po,int rows ,int offset) throws Exception {
        SqlParamsPairs sqlAndParams = ModelSqlUtils.getSearchFromObject(po,rows ,offset);
        //query for list
        List<T> list = getProxy().query(sqlAndParams.getSql(), sqlAndParams.getParams(),
                new BeanPropertyRowMapper(po.getClass()));
        if (list.size() > 0) {
            return list;
        } else {
            return null;
        }
    }
	
	/**
	 * 获取总行数
	 * get count
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	public int count(String sql, Object[] params) {
				
		int rowCount = 0;
		try{
			Map<String, Object> resultMap = null;
			if (params == null || params.length == 0) {
				resultMap = getProxy().queryForMap(sql);
			} else {
				resultMap = getProxy().queryForMap(sql, params);
			}
			Iterator<Map.Entry<String, Object>> it = resultMap.entrySet().iterator();
			if(it.hasNext()){
				Map.Entry<String, Object> entry = it.next();
				rowCount = ((Long)entry.getValue()).intValue();
			}
		}catch(EmptyResultDataAccessException e){
			
		}
		
		
		return rowCount;
	}

	/**
	 * 获取一个对象
	 * get object by id
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 * @throws NoIdAnnotationFoundException
	 * @throws NoColumnAnnotationFoundException
	 * @throws NoDefinedGetterException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T get(Class clazz, Object id) throws NoIdAnnotationFoundException, NoColumnAnnotationFoundException {
		
		//turn class to sql
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getGetFromObject(clazz, id);
		
		//query for list
		List<T> list = this.list(sqlAndParams.getSql(), sqlAndParams.getParams(), clazz);
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	// ---------------------------- update -----------------------------------//

	/**
	 * 更新某个对象
	 * update object
	 * @param po
	 * @throws Exception
	 */
	public void update(Object po) throws Exception {
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getUpdateFromObject(po);
		getProxy().update(sqlAndParams.getSql(), sqlAndParams.getParams());
	}

	
	/**
	 * 批量执行更新操作
	 * 
	 * @param sql
	 * @param paramsList
	 */
	public void batchUpdate(String sql,List<Object[]> paramsList){
		
		BatchUpdateSetter batchUpdateSetter = new BatchUpdateSetter(paramsList);
		
		getProxy().batchUpdate(sql, batchUpdateSetter);
	}

	/**
	 * 保存对象的快捷方法
	 * 如果Id标定的是自增会将自增长的主键自动设置回对象
	 * save object
	 * @param po
	 * @throws Exception
	 */
	public void save(Object po) throws Exception {
		String autoGeneratedColumnName = IdUtils.getAutoGeneratedId(po);
		if(!"".equals(autoGeneratedColumnName)){
			//有自增字段
			int idValue = save(po, autoGeneratedColumnName);
			//把自增的主键值再设置回去
			IdUtils.setAutoIncreamentIdValue(po,autoGeneratedColumnName,idValue);
		}else{
			SqlParamsPairs sqlAndParams = ModelSqlUtils.getInsertFromObject(po);
			
			getProxy().update(sqlAndParams.getSql(), sqlAndParams.getParams());
		}		
	}
	
	/**
	 * 保存对象并返回自增长主键的快捷方法
	 * 
	 * @param po
	 * @param autoGeneratedColumnName
	 *            自增长的主键的列名 比如 user_id
	 * @throws Exception
	 */
	private int save(Object po, String autoGeneratedColumnName) throws Exception {

		SqlParamsPairs sqlAndParams = ModelSqlUtils.getInsertFromObject(po);

		//动态切换库名
		String sql = sqlAndParams.getSql();
		
		return getProxy().insert(sql, sqlAndParams.getParams(), autoGeneratedColumnName);
	}
	
	//-------------------delete-----------------//
	public void delete(Object po) throws Exception{
		
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getDeleteFromObject(po);
		//动态切换库名
		String sql = sqlAndParams.getSql();
		
		getProxy().update(sql, sqlAndParams.getParams());	
	}

}
