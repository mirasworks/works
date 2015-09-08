package com.mirasworks.server.http;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.jboss.netty.handler.codec.http.HttpConstants.COLON;
import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.util.CharsetUtil;

public abstract class MessageEncoder extends OneToOneEncoder {

	private static final byte[] CRLF = new byte[] { CR, LF };
	private static final ChannelBuffer LAST_CHUNK = copiedBuffer("0\r\n\r\n", CharsetUtil.US_ASCII);

	private volatile boolean transferEncodingChunked;

	/**
	 * Creates a new instance.
	 */
	protected MessageEncoder() {
		super();
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg instanceof HttpMessage) {
			HttpMessage m = (HttpMessage) msg;
			boolean contentMustBeEmpty;
			if (m.isChunked()) {
				// if Content-Length is set then the message can't be HTTP
				// chunked
				if (isContentLengthSet(m)) {
					contentMustBeEmpty = false;
					transferEncodingChunked = false;
					removeTransferEncodingChunked(m);
				} else {
					// check if the Transfer-Encoding is set to chunked already.
					// if not add the header to the message
					if (!isTransferEncodingChunked(m)) {
						m.addHeader(Names.TRANSFER_ENCODING, Values.CHUNKED);
					}
					contentMustBeEmpty = true;
					transferEncodingChunked = true;
				}
			} else {
				transferEncodingChunked = contentMustBeEmpty = isTransferEncodingChunked(m);
			}

			ChannelBuffer header = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
			encodeInitialLine(header, m);
			encodeHeaders(header, m);
			header.writeByte(CR);
			header.writeByte(LF);

			ChannelBuffer content = m.getContent();
			if (!content.readable()) {
				return header; // no content
			} else if (contentMustBeEmpty) {
				throw new IllegalArgumentException(
						"HttpMessage.content must be empty " + "if Transfer-Encoding is chunked.");
			} else {
				return wrappedBuffer(header, content);
			}
		}

		if (msg instanceof HttpChunk) {
			HttpChunk chunk = (HttpChunk) msg;
			if (transferEncodingChunked) {
				if (chunk.isLast()) {
					transferEncodingChunked = false;
					if (chunk instanceof HttpChunkTrailer) {
						ChannelBuffer trailer = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
						trailer.writeByte((byte) '0');
						trailer.writeByte(CR);
						trailer.writeByte(LF);
						encodeTrailingHeaders(trailer, (HttpChunkTrailer) chunk);
						trailer.writeByte(CR);
						trailer.writeByte(LF);
						return trailer;
					} else {
						return LAST_CHUNK.duplicate();
					}
				} else {
					ChannelBuffer content = chunk.getContent();
					int contentLength = content.readableBytes();

					return wrappedBuffer(copiedBuffer(Integer.toHexString(contentLength), CharsetUtil.US_ASCII),
							wrappedBuffer(CRLF), content.slice(content.readerIndex(), contentLength),
							wrappedBuffer(CRLF));
				}
			} else {
				return chunk.getContent();
			}

		}

		// Unknown message type.
		return msg;
	}

	private static void encodeHeaders(ChannelBuffer buf, HttpMessage message) {
		try {
			for (Map.Entry<String, String> h : message.getHeaders()) {
				encodeHeader(buf, h.getKey(), h.getValue());
			}
		} catch (UnsupportedEncodingException e) {
			throw (Error) new Error().initCause(e);
		}
	}

	private static void encodeTrailingHeaders(ChannelBuffer buf, HttpChunkTrailer trailer) {
		try {
			for (Map.Entry<String, String> h : trailer.getHeaders()) {
				encodeHeader(buf, h.getKey(), h.getValue());
			}
		} catch (UnsupportedEncodingException e) {
			throw (Error) new Error().initCause(e);
		}
	}

	private static void encodeHeader(ChannelBuffer buf, String header, String value)
			throws UnsupportedEncodingException {
		buf.writeBytes(header.getBytes("ASCII"));
		buf.writeByte(COLON);
		buf.writeByte(SP);
		buf.writeBytes(value.getBytes("ASCII"));
		buf.writeByte(CR);
		buf.writeByte(LF);
	}

	static boolean isTransferEncodingChunked(HttpMessage m) {
		List<String> chunked = m.headers().getAll(HttpHeaders.Names.TRANSFER_ENCODING);
		if (chunked.isEmpty()) {
			return false;
		}

		for (String v : chunked) {
			if (v.equalsIgnoreCase(HttpHeaders.Values.CHUNKED)) {
				return true;
			}
		}
		return false;
	}

	static boolean isContentLengthSet(HttpMessage m) {
		List<String> contentLength = m.headers().getAll(HttpHeaders.Names.CONTENT_LENGTH);
		return !contentLength.isEmpty();
	}

	static void removeTransferEncodingChunked(HttpMessage m) {
		List<String> values = m.headers().getAll(HttpHeaders.Names.TRANSFER_ENCODING);
		if (values.isEmpty()) {
			return;
		}
		Iterator<String> valuesIt = values.iterator();
		while (valuesIt.hasNext()) {
			String value = valuesIt.next();
			if (value.equalsIgnoreCase(HttpHeaders.Values.CHUNKED)) {
				valuesIt.remove();
			}
		}
		if (values.isEmpty()) {
			m.headers().remove(HttpHeaders.Names.TRANSFER_ENCODING);
		} else {
			m.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, values);
		}
	}
	
    protected static void encodeAscii(String s, ChannelBuffer buf) {
        for (int i = 0; i < s.length(); i++) {
            buf.writeByte(s.charAt(i));
        }
    }

	protected abstract void encodeInitialLine(ChannelBuffer buf, HttpMessage message) throws Exception;

}
