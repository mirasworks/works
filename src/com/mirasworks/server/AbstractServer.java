package com.mirasworks.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractServer implements IServer {

	private final Logger l = LoggerFactory.getLogger(AbstractServer.class);
	private Integer port = null;

	
	
	@Override
	public void restart() {
		stop();
		start();
	}

	@Override
	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public int getPort() {
		if (port == null) {
			return getDefaultPort();
		} else {
			return getPort();
		}
	}
	@Override
	public void start() {
		l.info("server {} starting", getSeverTypeName());
	}
	
	@Override
	public void stop() {
		//TODO remove this  trace  when done
		l.info("server type {} not stoped properly because stop is not yet fully implemented", getSeverTypeName() );
	}

}
