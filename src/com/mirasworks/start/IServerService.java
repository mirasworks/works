package com.mirasworks.start;

public interface IServerService {

	IServer addServer(String key, IServer value);

	void start();

	void stop();

	void restart();

}
