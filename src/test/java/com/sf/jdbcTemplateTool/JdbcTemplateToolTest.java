package com.sf.jdbcTemplateTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.sf.jdbcTemplateTool.utils.InUtils;

@ContextConfiguration(locations = { "classpath*:/applicationContext.xml" })
@TransactionConfiguration(defaultRollback = false)
public class JdbcTemplateToolTest extends AbstractTransactionalJUnit4SpringContextTests{
    @Autowired
    public JdbcTemplateTool jtt;
    @Before
    public void before() throws Exception {
        jtt.execute("delete from t_user", null);
    }
    
    @Test
    public void execute() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('1','cloud1','34')", null);
        jtt.execute("update t_user set dob = ? where id = ? ", new Object[]{new Date(),'1'});
    }
    
    @Test()
    public void save() throws Exception {
        User user = new User();
        user.setId("2");
        user.setName("cloud2");
        user.setAge(new Random().nextInt(100));
        jtt.save(user);
    }
    
    @Test()
    public void updateByObject() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('3','cloud3','34')", null);
        User user = new User();
        user.setId("3");
        user.setDob(new Date());
        jtt.update(user);
    }
    
    
    @Test()
    public void list() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('4','cloud4','34')", null);
        List<UserInSearch> list = jtt.list("select * from t_user where name like ? limit ?,?", new Object[]{"%cloud%",1,2}, UserInSearch.class);
        for (User user : list) {
            System.out.println(user.getName());
        }
        List<String> ids = jtt.getJdbcTemplate().queryForList("select id from t_user",String.class);
        for (String id : ids) {
            System.out.println(id);
        }
    }
    
    @Test()
    public void listByObjectAndPagination() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('5','cloud5','34')", null);
        UserInSearch user = new UserInSearch();
        user.setName("cloud");
        user.setAgeStart(20);
        user.setAgeEnd(50);
        List<User> list = jtt.listWithPagination(user, 1, 0);
        for (User u : list) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
    }
    
    
    
    @Test()
    public void in() throws Exception {
        jtt.execute("insert into t_user(id,name,age) values ('6','cloud6',34)", null);
        jtt.execute("insert into t_user(id,name,age) values ('7','cloud7',35)", null);
        jtt.execute("insert into t_user(id,name,age) values ('8','cloud8',36)", null);
        String[] parms = new String[3];
        parms[0] = "6";
        parms[1] = "7";
        parms[2] = "8";
        UserInSearch user1 = new UserInSearch();
        user1.setId(InUtils.getStr4SQLINParam(parms));
        List<User> list1 = jtt.list(user1);
        for (User u : list1) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
        
        List<Integer> listParms = new ArrayList<Integer>();
        listParms.add(34);
        listParms.add(35);
        listParms.add(36);
        UserInSearch user2 = new UserInSearch();
        user2.setAgeIn(InUtils.getStr4SQLINParam(listParms));
        List<User> list2 = jtt.list(user2);       
        for (User u : list2) {
            System.out.println("name: "+u.getName()+" age: "+u.getAge());
        }
        
    }
}
