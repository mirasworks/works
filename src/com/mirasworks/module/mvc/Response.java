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

package com.mirasworks.module.mvc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.Cookie;
import com.mirasworks.util.DateUtil;

/**
 * 
 * @author modified from jetty rewrite still in progress
 *
 */
public class Response {
    private final Logger l = LoggerFactory.getLogger(Response.class);

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

    private int statusCode;

    /*
     * The object that will be rendered. Could be a Java Pojo. Or a map. Or xyz.
     * Will be handled by the TemplateReneringEngine.
     */
    private Object renderable;

    /**
     * Something like: "text/html" or "application/json"
     */
    private String contentType;

    /**
     * Something like: "utf-8" => will be appended to the content-type. eg
     * "text/html; charset=utf-8"
     */
    private String charset;

    private Map<String, String> headers;

    private List<Cookie> cookies;

    private String template;

    // TODO make a getter setter
    public ByteArrayOutputStream out;

    /**
     * A Response. Sets utf-8 as charset and status code by default. Refer to
     * {@link Response#SC_200_OK}, {@link Response#SC_204_NO_CONTENT} and so on
     * for some short cuts to predefined results.
     *
     * @param statusCode
     *            The status code to set for the Response. Shortcuts to the code
     *            at: {@link Response#SC_200_OK}
     */
    public Response() {
        // maybe a 500 by default instead
        this.statusCode = SC_200_OK;
        this.charset = "UTF-8";
        this.headers = new TreeMap<String, String>();
        this.cookies = new ArrayList<Cookie>();
    }

    /**
     * A Response. Sets utf-8 as charset and status code by default. Refer to
     * {@link Response#SC_200_OK}, {@link Response#SC_204_NO_CONTENT} and so on
     * for some short cuts to predefined results.
     *
     * @param statusCode
     *            The status code to set for the Response. Shortcuts to the code
     *            at: {@link Response#SC_200_OK}
     */
    public Response(int statusCode) {

        this.statusCode = statusCode;
        this.charset = "utf-8";

        this.headers = new TreeMap<String, String>();
        this.cookies = new ArrayList<Cookie>();

    }

    public Object getRenderable() {
        return renderable;
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
    public Response setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public Response addHeader(String headerName, String headerContent) {

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

    public Response addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public Response unsetCookie(String name) {
        // TODO get the cookie and remove the values
        l.error("unset cookie does nothing : not yet implemented");
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Set the status of this Response. Refer to {@link Response#SC_200_OK},
     * {@link Response#SC_204_NO_CONTENT} and so on for some short cuts to
     * predefined results.
     *
     * @param statusCode
     *            The status code. Response ({@link Response#SC_200_OK})
     *            provides some helpers.
     * @return The Response you executed the method on for method chaining.
     */
    public Response setStatus(int statusCode) {
        this.statusCode = statusCode;
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
    public Response setTemplate(String template) {
        this.template = template;
        return this;
    }

    public Response serve404() {
        setStatus(Response.SC_404_NOT_FOUND);
        return this;
    }

    public Response serve500() {
        setStatus(Response.SC_500_INTERNAL_SERVER_ERROR);
        return this;
    }
    
    public Response serve403() {
    	 setStatus(Response.SC_403_FORBIDDEN);
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
    public Response redirect(String url) {

        setStatus(Response.SC_303_SEE_OTHER);
        addHeader(Response.LOCATION, url);

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
    public Response redirectTemporary(String url) {

        setStatus(Response.SC_307_TEMPORARY_REDIRECT);
        addHeader(Response.LOCATION, url);

        return this;
    }

    /**
     * Set the content type of this Response to {@link Response#TEXT_HTML}.
     *
     * @return the same Response where you executed this method on. But the
     *         content type is now {@link Response#TEXT_HTML}.
     */
    public Response html() {
        contentType = TEXT_HTML;
        return this;
    }

    /**
     * Set the content type of this Response to
     * {@link Response#APPLICATION_JSON}.
     *
     * @return the same Response where you executed this method on. But the
     *         content type is now {@link Response#APPLICATION_JSON}.
     */
    public Response json() {
        contentType = APPLICATION_JSON;
        return this;
    }

    /**
     * Set the content type of this Response to
     * {@link Response#APPLICATION_JSONP}.
     *
     * @return the same Response where you executed this method on. But the
     *         content type is now {@link Response#APPLICATION_JSONP}.
     */
    public Response jsonp() {
        contentType = APPLICATION_JSONP;
        return this;
    }

    /**
     * Set the content type of this Response to {@link Response#TEXT_PLAIN}.
     *
     * @return the same Response where you executed this method on. But the
     *         content type is now {@link Response#TEXT_PLAIN}.
     */
    public Response text() {
        contentType = TEXT_PLAIN;
        return this;
    }

    /**
     * Set the content type of this Response to {@link Response#APPLICATON_XML}.
     *
     * @return the same Response where you executed this method on. But the
     *         content type is now {@link Response#APPLICATON_XML}.
     */
    public Response xml() {
        contentType = APPLICATION_XML;
        return this;
    }

    public Response file() {
        contentType = APPLICATION_OCTET_STREAM;
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
    public Response doNotCacheContent() {

        addHeader(CACHE_CONTROL, CACHE_CONTROL_DEFAULT_NOCACHE_VALUE);
        addHeader(DATE, DateUtil.formatForHttpHeader(System.currentTimeMillis()));
        addHeader(EXPIRES, DateUtil.formatForHttpHeader(0L));

        return this;

    }

}
