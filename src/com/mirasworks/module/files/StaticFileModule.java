package com.mirasworks.module.files;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.Context;
import com.mirasworks.server.Imodule;
import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.Ex400BadRequest;
import com.mirasworks.server.http.exceptions.Ex403Forbiden;
import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.server.http.exceptions.ExHttp;
import com.mirasworks.server.http.exceptions.ExNotMe;
import com.mirasworks.start.Application;

public class StaticFileModule implements Imodule {

	@SuppressWarnings("unused")
	private final Logger l = LoggerFactory.getLogger(StaticFileModule.class);

	static final String PUBLIC_DIR = Application.getConfig().getKey("public.directory", "public");
	static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	// static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	// static final int HTTP_CACHE_SECONDS = 60;

	public StaticFileModule(Context context) {
	}

	public WorksResponse serve(WorksRequest request) throws ExHttp {
		if (request.getMethod() != HttpMethod.GET) {
			throw new ExNotMe("not a get method");
		}

		final String path = sanitizeUri(request.getUri());
		if (path == null) {
			throw new Ex403Forbiden();
		}

		File file = new File(path);
		if (file.isHidden()) {
			throw new Ex403Forbiden();
		}
		if (!file.exists()) {
			throw new ExNotMe("file does not exists : " + path);
		}

		if (!file.isFile()) {
			throw new ExNotMe("not a file :"+ path);
		}

		// Cache Validation
		String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && ifModifiedSince.length() != 0) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
			Date ifModifiedSinceDate;
			try {
				ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
			} catch (ParseException e) {
				throw new Ex400BadRequest(
						"Bad date format. Format allowed : " + HTTP_DATE_FORMAT + " Locale " + Locale.US);
			}

			// Only compare up to the second because the datetime format we send
			// to the client does
			// not have milliseconds
			long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
			long fileLastModifiedSeconds = file.lastModified() / 1000;
			if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
				WorksResponse response = new WorksResponse(NOT_MODIFIED);
				response.setDateHeader();
				return response;
			}
		}

		WorksResponse response = new WorksResponse(OK);
		try {
			response.setFile(file);
		} catch (FileNotFoundException e) {
			throw new ExNotMe("set file failed");
		} catch (IOException e) {
			throw new Ex500();
		}
		return response;
	}

	/**
	 * 
	 * TODO #4 <a href= "https://github.com/mirasworks/works/issues/4">protect
	 * against url injection #4</a>
	 * 
	 * @param uri
	 * @return
	 * @throws Ex400BadRequest
	 */
	private static String sanitizeUri(String uri) throws Ex400BadRequest {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				throw new Ex400BadRequest();
			}
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		// Simplistic dumb security check.
		// http://projects.webappsec.org/w/page/13246949/Null%20Byte%20Injection
		// https://www.owasp.org/index.php/Double_Encoding
		// and many others
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".")
				|| uri.endsWith(".")) {

			throw new Ex400BadRequest();
		}

		// Convert to absolute path.
		return PUBLIC_DIR + File.separator + uri;
	}

}
