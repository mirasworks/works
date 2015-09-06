package com.mirasworks.start;

public interface IServers {

	IServer addServer(String key, IServer value);

	void start();

	void stop();

	void restart();

}
