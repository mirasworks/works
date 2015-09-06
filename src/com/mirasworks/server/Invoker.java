package com.mirasworks.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.http.WorksRequest;
import com.mirasworks.module.files.StaticFileModule;
import com.mirasworks.module.mvc.Response;
import com.mirasworks.server.http.exceptions.Ex403Forbiden;
import com.mirasworks.server.http.exceptions.ExNotMe;

public class Invoker {
	private final Logger l = LoggerFactory.getLogger(Invoker.class);
	private Context context;

	public Invoker(Context context) {
		this.context = context;

	}

	public Response invoke(WorksRequest request) {

		/**
		 * ^((http[s]?|ftp):\/)?\/?([^:\/\s]+)((\/\w+)*\/)([\w\-\.]+[^#?\s]+)(.*
		 * )?(#[\w\-]+)?$ TODO Here from now we are able to serve controller
		 * only controler.method we need to cache routes and serve either
		 * -static content -controller.method -404 -fav.ico Invoker should be
		 * renamed and used as modules
		 */

		try {

			ControllerInvokerModule controllerInvoker = new ControllerInvokerModule(context);
			try {
				return controllerInvoker.serve(request);
			} catch (ExNotMe e) {
				// nothing to do who's next ?
			}

			StaticFileModule staticInvoker = new StaticFileModule(context);
			try {
				return staticInvoker.serve(request);
			} catch (ExNotMe e) {
				// nothing to do who's next ?
			}

		} catch (Ex403Forbiden e) {
			return serveForbiden(request, e);
		} catch (Exception e) {
			return serve500(request, e);
		}

		return serve404(request);

	}

	private Response serveForbiden(WorksRequest request, Exception e) {
		// here better load a 500 or 404controller

		Response response = new Response();
		// TODO use template instead
		// TODO make template path configurable

		response.html().serve403();
		String html = "<h1>forbidden 403</h1><br>" + e.getMessage();
		try {
			response.out = new ByteArrayOutputStream();
			response.out.write(html.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			l.error("bad : unable to write 403", e);
		} catch (IOException e1) {
			l.error("bad : unable to write 403", e);
		}
		return response;
	}

	private Response serve500(WorksRequest request, Exception e) {

		Response response = new Response();
		// TODO use template instead
		// TODO make template path configurable

		response.html().serve500();
		String html = "<h1>500</h1><br>" + e.getMessage();
		try {
			response.out = new ByteArrayOutputStream();
			response.out.write(html.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			l.error("bad : unable to write 500", e);
		} catch (IOException e1) {
			l.error("bad : unable to write 500", e);
		}
		return response;
	}

	public Response serve404(WorksRequest request) {

		Response response = new Response();
		// TODO use template instead
		// TODO make template path configurable

		response.html().serve404();
		String html = "<h1>404</h1>";
		try {
			response.out = new ByteArrayOutputStream();
			response.out.write(html.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			l.error("bad : unable to write 404", e);
			serve500(request, e);
		} catch (IOException e) {
			l.error("bad : unable to write 404", e);
			serve500(request, e);
		}
		return response;

	}
}
