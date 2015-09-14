package com.mirasworks.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.exceptions.Ex400BadRequest;

/**
 * TODO make a route cache with that to avoid resolve the route many times TODO
 * load a route file
 *
 * @author Koda
 *
 */
public class Route {
	private final Logger l = LoggerFactory.getLogger(Route.class);
	private static String defaultControllerMethodName = "index";
	private static String defaultControllerName = "Index";

	private String controllerName = defaultControllerName;
	private String controllerMethodName = defaultControllerMethodName;
	private Map<String, List<String>> paramMap = null;

	public Route(WorksRequest request) throws Ex400BadRequest {

		String uri = request.getUri();
		String[] uriChunk = uri.split("/");

		int lenght = uriChunk.length;
		if (lenght > 1) {
			controllerName = StringUtils.capitalize(uriChunk[1]);
		}

		if (lenght > 2) {

			String lastPart = uriChunk[2];
			String[] lastPartChunck = lastPart.split("\\?");
			if (lastPartChunck.length > 0) {
				controllerMethodName = lastPartChunck[0];

				if (lastPartChunck.length > 1) {
					try {
						paramMap = splitQuery(lastPartChunck[1]);
						l.info("paramsMap: {}", paramMap);
					} catch (UnsupportedEncodingException e) {
						throw new Ex400BadRequest("unsuported encoding", e);
					}
				}

			} else if (lastPartChunck.length == 0) {
				controllerMethodName = uriChunk[2];

			}

		}

	}

	public static Map<String, List<String>> splitQuery(String params) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = params.split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1
					? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}

	public String getControllerName() {
		controllerName = StringUtils.capitalize(controllerName);
		return controllerName;
	}

	public String getMethodName() {
		return controllerMethodName;
	}

}
