package com.sf.jdbcTemplateTool;

import javax.persistence.Table;

import com.sf.jdbcTemplateTool.annotation.Operator;

@Table(name="t_user")
public class UserInSearch extends User {
    
    private Integer ageStart;
    private Integer ageEnd;
    
    private String ageIn;
    
    @Operator(value="in")
    public String getId() {
        return id;
    }
    @Operator(value="like")
    public String getName() {
        return name;
    }
    
    @Operator(targetColumn="age",value=">=")
    public Integer getAgeStart() {
        return ageStart;
    }
    @Operator(targetColumn="age",value="<=")
    public Integer getAgeEnd() {
        return ageEnd;
    }
    
    @Operator(value="in",targetColumn="age")
    public String getAgeIn() {
        return ageIn;
    }
    public void setAgeStart(Integer ageStart) {
        this.ageStart = ageStart;
    }
    public void setAgeEnd(Integer ageEnd) {
        this.ageEnd = ageEnd;
    }
    public void setAgeIn(String ageIn) {
        this.ageIn = ageIn;
    }
}
