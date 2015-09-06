package com.mirasworks.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.tools.commandparser.api.ISysInParser;

public class CommandParser implements ISysInParser {
	private final Logger l = LoggerFactory.getLogger(CommandParser.class);



	private IServers server = null;

	public CommandParser() {
		server = Application.getServerService();
	}

	
	@Override
	public String parse(String line) {

		if (line.contains("stop") || line.contains("q")) {
			server.stop();

		} else if (line.contains("start")) {
			server.restart();
			
		} else if (line.contains("restart")) {
			server.restart();
			
		} else if (line.isEmpty() == false) {
			l.info("unknown command : {}", line);
			
		}

		return null;
	}

}
