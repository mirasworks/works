package com.mirasworks.server;

public interface IServer {

	void start();

	void stop();

	void restart();

	void setPort(Integer port);

	int getDefaultPort();

	int getPort();

	String getSeverTypeName();
}
