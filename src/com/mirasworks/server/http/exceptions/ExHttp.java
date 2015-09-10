package com.mirasworks.server.http.exceptions;

public class ExHttp extends Exception {

	private static final long serialVersionUID = -8659365270747453088L;

	public ExHttp() {

	}

	public ExHttp(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExHttp(String message, Throwable cause) {
		super(message, cause);
	}

	public ExHttp(String message) {
		super(message);
	}

	public ExHttp(Throwable cause) {
		super(cause);
	}
}
