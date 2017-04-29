package com.sf.jdbcTemplateTool.exception;

import java.lang.reflect.Method;

public class NoColumnAnnotationFoundException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public NoColumnAnnotationFoundException(String ClassName,Method getter){
		super(ClassName + "." + getter.getName() + "() should have an @Column annotation.");
	}
}
