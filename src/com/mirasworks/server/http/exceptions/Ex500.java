package com.mirasworks.server.http.exceptions;

public class Ex500 extends Exception {

	private static final long serialVersionUID = -4654026861911504856L;

	public Ex500() {
		super();
	
	}

	public Ex500(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Ex500(String message, Throwable cause) {
		super(message, cause);
	}

	public Ex500(String message) {
		super(message);
	}

	public Ex500(Throwable cause) {
		super(cause);
	}

}
