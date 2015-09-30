package com.mirasworks.start;

import com.mirasworks.configuration.api.ConfigurationManager;
import com.mirasworks.tools.clia.impl.CliApplication;

/**
 * 
 * Damien MIRAS
 *
 */
public class Application extends CliApplication {

    private static IServers serverService = null;
    private static ConfigurationManager config = new ConfigurationManager();

    public static IServers getServerService() {
        return serverService;
    }



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
