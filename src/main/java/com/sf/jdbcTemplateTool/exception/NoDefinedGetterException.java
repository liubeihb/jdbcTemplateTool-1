package com.sf.jdbcTemplateTool.exception;

public class NoDefinedGetterException extends Exception {
	private static final long serialVersionUID = 1L;
	private String fieldName;
	
	public NoDefinedGetterException(String fieldName){
		super(fieldName + " should have an getter method.");
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
}
