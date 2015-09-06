package com.mirasworks.start;

import com.mirasworks.configuration.api.ConfigurationManager;
import com.mirasworks.tools.clia.impl.CliApplication;

/**
 * 
 * @author Koda
 *
 */
public class Application extends CliApplication {

    private static IServerService serverService = null;
    private static ConfigurationManager config = new ConfigurationManager();

    public static IServerService getServerService() {
        return serverService;
    }


    //TODO Damien see config
    //https://www.playframework.com/documentation/1.2.4/configuration
    //for configuration we should handle
    public static ConfigurationManager getConfig() {
        return config;
    }

    public Application(String args[]) {

        if (initApp(config)) {

            setSysInParser(new CommandParser());
            setCommandParser(new CliCommandParserWorks());
            this.processCommand(args);
            startListening();

        } else {
            System.err.println("config failed");
        }
    }


    @Override
    public String getApplicationName() {
        return "works";
    }

    @Override
    public boolean initApp(ConfigurationManager config) {

        if (!super.initApp(config)) {
            return false;
        }

        serverService = new ServerManager();

        return true;

    }

}
