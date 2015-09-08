package com.mirasworks.server.http;
import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import com.mirasworks.server.http.MessageEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class ResponseEncoder extends MessageEncoder {

    @Override
    protected void encodeInitialLine(ChannelBuffer buf, HttpMessage message) throws Exception {
        HttpResponse response = (HttpResponse) message;
        encodeAscii(response.getProtocolVersion().toString(), buf);
        buf.writeByte(SP);
        encodeAscii(String.valueOf(response.getStatus().getCode()), buf);
        buf.writeByte(SP);
        encodeAscii(String.valueOf(response.getStatus().getReasonPhrase()), buf);
        buf.writeByte(CR);
        buf.writeByte(LF);
    }
}
