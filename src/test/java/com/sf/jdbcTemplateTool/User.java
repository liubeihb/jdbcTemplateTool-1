package com.sf.jdbcTemplateTool;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Table;

import com.sf.jdbcTemplateTool.model.Pagination;

@Table(name="t_user")
public class User extends Pagination {

    protected String id;
    protected String name;
    protected Integer age;
    protected Date dob;

    @Id
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public Date getDob() {
        return dob;
    }
    public void setDob(Date dob) {
        this.dob = dob;
    }
    
    
}
