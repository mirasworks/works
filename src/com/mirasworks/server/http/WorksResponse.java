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

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.util.DateUtil;

/**
 * 
 * @author modified from jetty rewrite still in progress
 *
 */
public class WorksResponse {
	private final Logger l = LoggerFactory.getLogger(WorksResponse.class);

	// /////////////////////////////////////////////////////////////////////////
	// HTTP Status codes (for convenience)
	// /////////////////////////////////////////////////////////////////////////
	public static final int SC_200_OK = 200;
	public static final int SC_201_CREATED = 201;
	public static final int SC_204_NO_CONTENT = 204;

	// for redirects:
	public static final int SC_300_MULTIPLE_CHOICES = 300;
	public static final int SC_301_MOVED_PERMANENTLY = 301;
	public static final int SC_302_FOUND = 302;
	public static final int SC_303_SEE_OTHER = 303;
	public static final int SC_304_NOT_MODIFIED = 304;
	public static final int SC_307_TEMPORARY_REDIRECT = 307;

	public static final int SC_400_BAD_REQUEST = 400;
	public static final int SC_401_UNAUTHORIZED = 401;
	public static final int SC_403_FORBIDDEN = 403;
	public static final int SC_404_NOT_FOUND = 404;

	public static final int SC_500_INTERNAL_SERVER_ERROR = 500;
	public static final int SC_501_NOT_IMPLEMENTED = 501;

	// /////////////////////////////////////////////////////////////////////////
	// Some MIME types (for convenience)
	// /////////////////////////////////////////////////////////////////////////
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String APPLICATION_JSON = "application/json";
	/* @deprecated Naming mistake - Please use APPLICATION_JSON instead! */

	public static final String APPLICATION_JSONP = "application/javascript";
	/* @deprecated Naming mistake - Please use APPLICATION_JSONP instead! */

	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	// /////////////////////////////////////////////////////////////////////////
	// Finally we got to the core of this class...
	// /////////////////////////////////////////////////////////////////////////
	/* Used as redirection header */
	public static final String LOCATION = "Location";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CACHE_CONTROL_DEFAULT_NOCACHE_VALUE = "no-cache, no-store, max-age=0, must-revalidate";

	public static final String DATE = "Date";
	public static final String EXPIRES = "Expires";

	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	private String contentType;

	private String charset;

	private Map<String, String> headers;

	private List<Cookie> cookies;

	private String template;
	private HttpVersion version;

	// TODO make a getter setter
	public ByteArrayOutputStream out;

	private HttpResponseStatus status;

	public WorksResponse(HttpResponseStatus status) {

		this();
		// always 1.1 it will not changes tomorrow
		setVersion(HTTP_1_1);
		setStatus(status);
		
	}

	public WorksResponse() {
		this.charset = "UTF-8";
		this.headers = new TreeMap<String, String>();
		this.cookies = new ArrayList<Cookie>();
	}

	public HttpResponseStatus getStatus() {
		return status;
	}

	public WorksResponse setStatus(HttpResponseStatus status) {
		if (status == null) {
			throw new NullPointerException("status");
		}
		this.status = status;
		return this;
	}

	public void setVersion(HttpVersion version) {
		if (version == null) {
			throw new NullPointerException("version");
		}
		this.version = version;
	}

	public String getContentType() {
		return contentType;
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

	public WorksResponse addHeader(String headerName, String headerContent) {

		headers.put(headerName, headerContent);
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
	 * Set the template to render. For instance
	 * template("views/AnotherController/anotherview.ftl.html");
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
		addHeader(WorksResponse.LOCATION, url);

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
		addHeader(WorksResponse.LOCATION, url);

		return this;
	}

	/**
	 * Set the content type of this Response to {@link WorksResponse#TEXT_HTML}.
	 * @param string 
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#TEXT_HTML}.
	 * @throws Ex500 
	 */
	public WorksResponse html(String html) throws Ex500 {
		contentType = TEXT_HTML;
		// TODO use template instead
		// TODO make template path configurable
		return writeString(html);
		
	}
	
	private  WorksResponse writeString(String string) throws Ex500 {
	
		try {
			out = new ByteArrayOutputStream();
			out.write(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new Ex500("unable to write html beacause of bad encoding", e.getCause());
		} catch (IOException e) {
			throw new Ex500("unable to write content beacause of io error", e.getCause());
		}finally {
			//TODO Close
		}
		
		return this;
	}

	/**
	 * Set the content type of this Response to
	 * {@link WorksResponse#APPLICATION_JSON}.
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#APPLICATION_JSON}.
	 */
	public WorksResponse json() {
		contentType = APPLICATION_JSON;
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
		contentType = APPLICATION_JSONP;
		return this;
	}

	/**
	 * Set the content type of this Response to {@link WorksResponse#TEXT_PLAIN}
	 * .
	 * @param string 
	 *
	 * @return the same Response where you executed this method on. But the
	 *         content type is now {@link WorksResponse#TEXT_PLAIN}.
	 * @throws Ex500 
	 */
	public WorksResponse text(String text) throws Ex500 {
		contentType = TEXT_PLAIN;
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
		contentType = APPLICATION_XML;
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

		addHeader(CACHE_CONTROL, CACHE_CONTROL_DEFAULT_NOCACHE_VALUE);
		addHeader(DATE, DateUtil.formatForHttpHeader(System.currentTimeMillis()));
		addHeader(EXPIRES, DateUtil.formatForHttpHeader(0L));

		return this;

	}

}
