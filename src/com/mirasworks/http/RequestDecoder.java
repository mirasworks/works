package com.mirasworks.http;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDecoder extends HttpRequestDecoder {
    @SuppressWarnings("unused")
    private final Logger l = LoggerFactory.getLogger(RequestDecoder.class);

    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {

        WorksRequest httpRequest = new WorksRequest(HttpVersion.valueOf(initialLine[2]), HttpMethod.valueOf(initialLine[0]), initialLine[1]);
        return httpRequest;
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

}
