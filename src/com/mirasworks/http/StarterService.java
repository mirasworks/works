package com.mirasworks.http;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.IServer;
import com.mirasworks.start.IServerService;

public class StarterService implements IServerService {
    private final Logger l = LoggerFactory.getLogger(StarterService.class);

    private SortedMap<String, IServer> servers = new TreeMap<String, IServer>();

    private boolean isStarted = false;

    public StarterService() {
        addServer("http", new Server());

    }

    /**
    * Add new server
    *
    * @param key
    * @param value
    * @return
    */
    @Override
    public IServer addServer(String key, IServer value) {
        return servers.put(key, value);
    }

    /**
    * start all servers
    */
    @Override
    public void start() {

        System.setProperty("file.encoding", "utf-8");
        l.info("try to start");

        for (Map.Entry<String, IServer> server : servers.entrySet()) {
            server.getValue().start();
        }
        isStarted = true;

    }

    /**
    * stop all servers
    */
    @Override
    public void stop() {
        l.info("try to stop");
        for (Map.Entry<String, IServer> server : servers.entrySet()) {
            server.getValue().stop();
        }
    }

    /**
    * restart all servers
    */
    @Override
    public void restart() {
        if (isStarted == false) {
            start();
        } else {
            for (Map.Entry<String, IServer> server : servers.entrySet()) {
                server.getValue().restart();
            }

        }
    }
}