package com.app.exception;

public class InvalidKpiWeightException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidKpiWeightException(String msg) {
        super(msg);
    }
}
