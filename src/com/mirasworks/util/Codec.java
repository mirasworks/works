package com.mirasworks.util;

import java.util.Iterator;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;

public class Codec {


	public static boolean isTransferEncodingChunked(HttpMessage httpMessage) {
		List<String> chunked = httpMessage.headers().getAll(HttpHeaders.Names.TRANSFER_ENCODING);
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

	public static boolean isContentLengthSet(HttpMessage httpMessage) {
		List<String> contentLength = httpMessage.headers().getAll(HttpHeaders.Names.CONTENT_LENGTH);
		return !contentLength.isEmpty();
	}

	public static void removeTransferEncodingChunked(HttpMessage worksReponse) {
		List<String> values = worksReponse.headers().getAll(HttpHeaders.Names.TRANSFER_ENCODING);
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
			worksReponse.headers().remove(HttpHeaders.Names.TRANSFER_ENCODING);
		} else {
			worksReponse.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, values);
		}
	}

}
