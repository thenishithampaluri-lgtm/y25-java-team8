package com.app.exception;

public class MissingKpiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingKpiException(String msg) {
        super(msg);
    }
}
