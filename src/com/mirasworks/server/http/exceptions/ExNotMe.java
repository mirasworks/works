package com.mirasworks.server.http.exceptions;

public class ExNotMe extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -384105005367871707L;

	public ExNotMe() {
		super();
	}

	public ExNotMe(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ExNotMe(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ExNotMe(String arg0) {
		super(arg0);
	}

	public ExNotMe(Throwable arg0) {
		super(arg0);
	}



}
