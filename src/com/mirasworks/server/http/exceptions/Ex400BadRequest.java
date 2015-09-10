package com.mirasworks.server.http.exceptions;

/**
 * 
 * @author Damien MIRAS Bad request
 *
 */
public class Ex400BadRequest extends FastRuntimeException {

	private static final long serialVersionUID = 3359304929198952571L;

	public Ex400BadRequest() {
		super();

	}

	public Ex400BadRequest(String message, Throwable cause) {
		super(message, cause);
	}

	public Ex400BadRequest(String message) {
		super(message);
	}

	public Ex400BadRequest(Throwable cause) {
		super(cause);
	}

}
