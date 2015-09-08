package com.mirasworks.server.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.Context;
import com.mirasworks.server.Invoker;

/**
 *
 * @author Koda
 *         here we build the http response
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {

    private static Logger l = LoggerFactory.getLogger(ServerHandler.class);

    private WorksRequest worksRequest;

    private boolean readingChunks = false;

    private final StringBuilder strBuff = new StringBuilder();

    private Context context = null;;

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

        // TODO create a security handeler to catch too long request
        // TODO handle file content type
        // TODO close the handler as soon as possible with use of future
        // TODO request and cookie
        // could be used to upload png etc
        // wrap a new request and close the handler

        if (readingChunks == false) {
            // TODO may be consider to directly create a WorksRequest
            /**
             * @see RequestDecoder
             *
             * @see DefaultHttpRequest
             */
            this.worksRequest = (WorksRequest) e.getMessage();
            l.info("{}",worksRequest);

            if (is100ContinueExpected(worksRequest)) {
                send100Continue(e);
            }

            // important the buff is reused along the new requests instead
            strBuff.setLength(0);

            // TODO may consider a thread safe pool of queryStringdecoder
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(worksRequest.getUri());
            Map<String, List<String>> params = queryStringDecoder.getParameters();

            worksRequest.setParams(params);

            if (worksRequest.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer channelbufferContent = worksRequest.getContent();
                if (channelbufferContent.readable()) {
                    // TODO here handle files
                    // InputStream contentInputStream = null;
                    // ByteArrayOutputStream out = new ByteArrayOutputStream();
                    // IOUtils.copy(new
                    // ChannelBufferInputStream(channelbufferContent), out);
                    // byte[] n = out.toByteArray();
                    // contentInputStream = new ByteArrayInputStream(n);

                    // TODO here handle content type and uft8 see the RFC
                    worksRequest.setContent(channelbufferContent);

                }

                writeResponse(e);
            }
        } else {
            l.warn("Chunked content not yet supported");
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
            }
        }
    }


    /**
     *
     * @param context
     */
    //what a shame so hugly
    public void setContext(Context context) {
        this.context  = context;
    }

    private void writeResponse(MessageEvent messageEvent) {
        // Decide whether to close the connection or not

        boolean keepAlive = isKeepAlive(worksRequest);
        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        WorksResponse WorksResponse = null;
        Invoker invoker = new Invoker(context);
        WorksResponse = invoker.invoke(worksRequest);

        
        response.setContent(ChannelBuffers.copiedBuffer(WorksResponse.out.toByteArray()));

        StringBuffer contentType = new StringBuffer();
        contentType.append(WorksResponse.getContentType());
        contentType.append("; charset=");
        contentType.append(WorksResponse.getCharset());
        response.headers().set(CONTENT_TYPE, contentType.toString());

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.getContent().readableBytes());
			// Add keep alive header as per:
			// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }


        //write and close if not keep alive 
        ChannelFuture future = messageEvent.getChannel().write(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    


    private static void send100Continue(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (this == ctx.getPipeline().getLast()) {
            l.warn("http handler has closed a connection cause {}", e.getCause(), e);
        }
        ctx.sendUpstream(e);
    }

}
