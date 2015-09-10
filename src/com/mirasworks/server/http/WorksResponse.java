/**
 * Copyright (C) 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirasworks.server.http;

//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.start.Application;
import com.mirasworks.util.DateUtil;

/**
 * 
 * @author modified from jetty rewrite still in progress
 *
 */
public class WorksResponse extends DefaultHttpResponse {
	private final Logger l = LoggerFactory.getLogger(WorksResponse.class);

	// /////////////////////////////////////////////////////////////////////////
	// Some MIME types (for convenience)
	// /////////////////////////////////////////////////////////////////////////
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_JSONP = "application/javascript";
	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";


	/* Used as redirection header */
	public static final String LOCATION = "Location";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CACHE_CONTROL_DEFAULT_NOCACHE_VALUE = "no-cache, no-store, max-age=0, must-revalidate";

	public static final String DATE = "Date";
	public static final String EXPIRES = "Expires";

	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String defaultCharset = Application.getConfig().getKey("charset", "UTF-8");

	// private String contentType;

	private String charset;

	private List<Cookie> cookies;

	private String template;

	// TODO make a getter setter
	public ByteArrayOutputStream bytearrayOutputStream;

	public WorksResponse() {
		super(HTTP_1_1, HttpResponseStatus.OK);
		this.charset = defaultCharset;
		this.cookies = new ArrayList<Cookie>();
	}

	public WorksResponse(HttpResponseStatus status) {

		super(HTTP_1_1, status);
		this.charset = defaultCharset;
		this.cookies = new ArrayList<Cookie>();
	}

	private WorksResponse writeString(String string) throws Ex500 {

		try {
			bytearrayOutputStream = new ByteArrayOutputStream();
			bytearrayOutputStream.write(string.getBytes(getCharset()));
			setContent(ChannelBuffers.copiedBuffer(bytearrayOutputStream.toByteArray()));
		} catch (UnsupportedEncodingException e) {
			throw new Ex500("unable to write html beacause of bad encoding", e.getCause());
		} catch (IOException e) {
			throw new Ex500("unable to write content beacause of io error", e.getCause());
		} finally {
			// TODO Close
			try {
				bytearrayOutputStream.close();
			} catch (IOException e) {

			}
		}

		return this;
	}

	/**
	 * @return Charset of the current Response that will be used. Will be
	 *         "utf-8" by default.
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            Set the charset of the Response. Is "utf-8" by default.
	 * @return The Response for chaining.
	 */
	public WorksResponse setCharset(String charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Returns cookie with that name or null.
	 *
	 * @param cookieName
	 *            Name of the cookie
	 * @return The cookie or null if not found.
	 */
	public Cookie getCookie(String cookieName) {

		for (Cookie cookie : getCookies()) {
			if (cookie.getName().equals(cookieName)) {
				return cookie;
			}
		}

		return null;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public WorksResponse addCookie(Cookie cookie) {
		cookies.add(cookie);
		return this;
	}

	public WorksResponse unsetCookie(String name) {
		// TODO get the cookie and remove the values
		l.error("unset cookie does nothing : not yet implemented");
		return this;
	}

	public String getTemplate() {
		return template;
	}

	/**
	 * Set content type for char text typed content type
	 * 
	 * @param contentType
	 */
	private void setCharContentType(String contentType) {
		StringBuffer contentTypeStrb = new StringBuffer();
		contentTypeStrb.append(contentType);
		contentTypeStrb.append("; charset=");
		contentTypeStrb.append(getCharset());
		headers().set(HttpHeaders.Names.CONTENT_TYPE, contentTypeStrb.toString());
	}

	/**
	 * for keepAlive only content Add 'Content-Length' header only for a
	 * keep-alive connection. Add keep alive header as per Connection: <a href
	 * ="http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#">
	 * rfc 2616</a> 
	 */
	public void setKeepAliveHeaders() {

		headers().set(HttpHeaders.Names.CONTENT_LENGTH, getContent().readableBytes());
		headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
	}

	/**
	 * Set the template to render. For instance
	 * template("views/AnotherController/anotherview.html");
	 *
	 * @param template
	 *            The view to render. Eg.
	 *            views/AnotherController/anotherview.ftl.html
	 * @return The Response that you executed the method on for chaining.
	 */
	public WorksResponse setTemplate(String template) {
		this.template = template;
		return this;
	}

	/**
	 * A redirect that uses 303 see other.
	 *
	 * @param url
	 *            The url used as redirect target.
	 * @return A nicely configured Response with status code 303 and the url set
	 *         as Location header.
	 */
	public WorksResponse redirect(String url) {

		setStatus(HttpResponseStatus.SEE_OTHER);
		headers().add(WorksResponse.LOCATION, url);

		return this;
	}

	/**
	 * A redirect that uses 307 see other.
	 *
	 * @param url
	 *            The url used as redirect target.
	 * @return A nicely configured Response with status code 307 and the url set
	 *         as Location header.
	 */
	public WorksResponse redirectTemporary(String url) {

		setStatus(HttpResponseStatus.TEMPORARY_REDIRECT);
		headers().add(WorksResponse.LOCATION, url);

		return this;
	}

	/**
	 * Set the content type of this Response to {@link WorksResponse#TEXT_HTML}.
	 * 
	 * @param string
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#TEXT_HTML}.
	 * @throws Ex500
	 */
	public WorksResponse html(String html) throws Ex500 {
		setCharContentType(TEXT_HTML);
		// TODO use template instead
		// TODO make template path configurable
		return writeString(html);

	}

	/**
	 * Set the content type of this Response to
	 * {@link WorksResponse#APPLICATION_JSON}.
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#APPLICATION_JSON}.
	 */
	public WorksResponse json() {
		setCharContentType(APPLICATION_JSON);
		return this;
	}

	/**
	 * Set the content type of this Response to
	 * {@link WorksResponse#APPLICATION_JSONP}.
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#APPLICATION_JSONP}.
	 */
	public WorksResponse jsonp() {
		setCharContentType(APPLICATION_JSONP);
		return this;
	}

	/**
	 * Set the content type of this Response to {@link WorksResponse#TEXT_PLAIN}
	 * .
	 * 
	 * @param string
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#TEXT_PLAIN}.
	 * @throws Ex500
	 */
	public WorksResponse text(String text) throws Ex500 {
		setCharContentType(TEXT_PLAIN);
		writeString(text);
		return this;
	}

	/**
	 * Set the content type of this Response to
	 * {@link WorksResponse#APPLICATON_XML}.
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#APPLICATON_XML}.
	 */
	public WorksResponse xml() {
		setCharContentType(APPLICATION_XML);
		return this;
	}

	// TODO here lack FileType contentType and others

	/**
	 * This function sets
	 *
	 * Cache-Control: no-cache, no-store Date: (current date) Expires: 1970
	 *
	 * => it therefore effectively forces the browser and every proxy in between
	 * not to cache content.
	 *
	 * See also https://devcenter.heroku.com/articles/increasing-application-
	 * performance-with-http-cache-headers
	 *
	 * @return this Response for chaining.
	 */
	public WorksResponse doNotCacheContent() {

		headers().add(CACHE_CONTROL, CACHE_CONTROL_DEFAULT_NOCACHE_VALUE);
		headers().add(DATE, DateUtil.formatForHttpHeader(System.currentTimeMillis()));
		headers().add(EXPIRES, DateUtil.formatForHttpHeader(0L));

		return this;

	}

}
