package com.mirasworks.server.http;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.ssl.SslContext;

import com.mirasworks.server.Context;

public class PipelineFactory implements ChannelPipelineFactory {

	private final Context context;

	public PipelineFactory(Context context) {
		this.context = context;
	}

	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = Channels.pipeline();

		SslContext sslCtx = context.getSslCtx();
		if (sslCtx != null) {
			pipeline.addLast("ssl", sslCtx.newHandler());
		}

		ServerHandler httpServerHandler = new ServerHandler();
		httpServerHandler.setContext(context);

		// Uncomment the following line if you want HTTPS
		// SSLEngine engine =
		// SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);
		// pipeline.addLast("ssl", new SslHandler(engine));

		// TODO See play to separate the chunck writer
		pipeline.addLast("flashPolicy", new FlashPolicyHandler());
		// simple netty ineritance to debug
		pipeline.addLast("decoder", new RequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		// pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		// TODO rewrite the httpResponse encoder to handle a worksResponse and
		// avoid the copy
		pipeline.addLast("encoder", new ResponseEncoder());
		pipeline.addLast("handler", httpServerHandler);
		// Remove the following line if you don't want automatic content
		// compression.
		// pipeline.addLast("deflater", new HttpContentCompressor());
		// uncomment to see the request back
		return pipeline;
	}
}
