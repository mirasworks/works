package com.mirasworks.server.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.Context;
import com.mirasworks.server.Invoker;

/**
 *
 * @author Koda here we build the http response
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
		// TODO request and cookie
		// could be used to upload png etc

		if (readingChunks == false) {

			this.worksRequest = (WorksRequest) e.getMessage();

			if (is100ContinueExpected(worksRequest)) {
				send100Continue(e);
			}

			// important the buff is reused along the new requests instead
			strBuff.setLength(0);

			// TODO may consider a thread safe pool of queryStringdecoder.
			//
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
	// what a shame so hugly
	public void setContext(Context context) {
		this.context = context;
	}

	private void writeResponse(MessageEvent messageEvent) {

		// Decide whether to close the connection or not
		boolean keepAlive = isKeepAlive(worksRequest);

		// let the framework build the response object.
		WorksResponse WorksResponse = null;
		Invoker invoker = new Invoker(context);
		WorksResponse = invoker.invoke(worksRequest);
		final RandomAccessFile randomAcessFile = WorksResponse.getRandomAcessFile();

		if (keepAlive ) {
			WorksResponse.setKeepAliveHeaders();
		}

		Channel Channel = messageEvent.getChannel();

		// Even if that is a file we need to Write the initial line and the
		// header.
		ChannelFuture future = Channel.write(WorksResponse);

	

		Long fileLenght = WorksResponse.getFileLength();
		if (randomAcessFile != null) {

			if (fileLenght != null) {
				long lenght = fileLenght.longValue();

				// Write the content.
				ChannelFuture fileFuture;
				if (Channel.getPipeline().get(SslHandler.class) != null) {
					try {
						// Cannot use zero-copy with HTTPS.
						fileFuture = Channel.write(new ChunkedFile(randomAcessFile, 0, lenght, 8192));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// No encryption - use zero-copy.
					final FileRegion region = new DefaultFileRegion(randomAcessFile.getChannel(), 0, lenght);
					fileFuture = Channel.write(region);
					final String path = worksRequest.getUri();
					fileFuture.addListener(new ChannelFutureProgressListener() {
						public void operationComplete(ChannelFuture future) {
							region.releaseExternalResources();
							try {
								randomAcessFile.close();
							} catch (IOException e) {

								l.error("can't close the randomAcessFile after use", e);
							}
						}

						public void operationProgressed(ChannelFuture future, long amount, long current, long total) {
							System.err.printf("%s: %d / %d (+%d)%n", path, current, total, amount);
						}
					});
				}

			} else {
				l.error("random acess file was instanciated but response handle no lenght");
				try {
					randomAcessFile.close();
				} catch (IOException e) {

					l.error("can't close the randomAcessFile", e);
				}
			}
		}

		
		
		// write and close if not keep alive
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
			l.error("http handler has closed a connection cause {} {} {}", e.getCause(), e);
		}
		ctx.sendUpstream(e);
	}

}
