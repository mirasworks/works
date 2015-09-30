package com.mirasworks.start;

import org.apache.commons.cli.CommandLine;

import com.mirasworks.tools.clia.impl.CliCommandParser;

/**
 * 
 * Damien MIRAS
 *         This class is in WIP and have to be tuned but global idea is here
 * 
 * 
 */
public class CliCommandParserWorks extends CliCommandParser {

	private IServers server = null;

	public CliCommandParserWorks() {
		server  = Application.getServerService();
	}

	@Override
	protected void initOptions() {

		options.addOption("start", false, "start all servers server");
		options.addOption("stop", false, "stop all servers server");
		options.addOption("restart", false, "restart all servers server");
	}

	@Override
	protected void internalParseCmd(CommandLine cmd) {

		// TODO do mutual exclusion see cli doc
		if (cmd.hasOption("start")) {
			server.start();
		}
		if (cmd.hasOption("stop")) {
			server.stop();
		}
		if (cmd.hasOption("restart")) {
			server.restart();
		}

	}

}
