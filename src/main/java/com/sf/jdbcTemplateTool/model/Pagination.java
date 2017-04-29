package com.sf.jdbcTemplateTool.model;

import javax.persistence.Transient;

import com.sf.jdbcTemplateTool.annotation.Ignore;

public class Pagination {
    private int rows;
    private int pageNum;
    
    @Transient
    @Ignore
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    @Transient
    @Ignore
    public int getPageNum() {
        return pageNum;
    }
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    
}
