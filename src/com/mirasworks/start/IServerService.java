package com.mirasworks.start;

import com.mirasworks.server.IServer;

public interface IServerService {

	IServer addServer(String key, IServer value);

	void start();

	void stop();

	void restart();

}
