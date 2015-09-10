package com.mirasworks.server.http.exceptions;

/**
 * from playframework 1.3 : Fast Exception - skips creating stackTrace.
 *
 * More info here: <a>http://www.javaspecialists.eu/archive/Issue129.html</a>
 */
public class FastRuntimeException extends ExHttp {

	private static final long serialVersionUID = -4813280710509731608L;

	public FastRuntimeException() {
		super();
	}

	public FastRuntimeException(String desc) {
		super(desc);
	}

	public FastRuntimeException(String desc, Throwable cause) {
		super(desc, cause);
	}

	public FastRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Since we override this method, no stacktrace is generated - much faster
	 * 
	 * @return always null
	 */
	public Throwable fillInStackTrace() {
		return null;
	}
}
