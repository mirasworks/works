package com.mirasworks.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.http.WorksRequest;

/**
 * TODO make a route cache with that to avoid resolve the route many times
 * TODO load a route file
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

    public Route(WorksRequest request) {

        String uri  = request.getUri();
        String[] uriChunk = request.getUri().split("/");

        l.info("uri in route resolver: {}", uri);

        int lenght = uriChunk.length;
        if (lenght > 1) {
            controllerName = StringUtils.capitalize(uriChunk[1]);
        }

        if (lenght > 2) {
            controllerMethodName = uriChunk[2];
        }

    }

    public String getControllerName() {
        l.info(controllerName);
        controllerName = StringUtils.capitalize(controllerName);
        return controllerName;
    }

    public String getMethodName() {
        return controllerMethodName;
    }

}
