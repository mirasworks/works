package com.mirasworks.server;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.module.files.StaticFileModule;
import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.Ex403Forbiden;
import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.server.http.exceptions.ExNotMe;

public class Invoker {
	private final Logger l = LoggerFactory.getLogger(Invoker.class);
	private Context context;

	public Invoker(Context context) {
		this.context = context;

	}

	public WorksResponse invoke(WorksRequest request) {

		/**
		 * ^((http[s]?|ftp):\/)?\/?([^:\/\s]+)((\/\w+)*\/)([\w\-\.]+[^#?\s]+)(.*
		 * )?(#[\w\-]+)?$ TODO Here from now we are able to serve controller
		 * only controler.method we need to cache routes and serve either
		 * -static content -controller.method -404 -fav.ico Invoker should be
		 * renamed and used as modules
		 */

		try {

			ControllerInvokerModule controllerModule = new ControllerInvokerModule(context);
			try {
				return controllerModule.serve(request);
			} catch (ExNotMe e) {
				l.debug("ControllerInvokerModule pass: {}", e.getMessage());
			}

			StaticFileModule staticModule = new StaticFileModule(context);
			try {
				return staticModule.serve(request);
			} catch (ExNotMe e) {
				l.debug("StaticFileModule pass: {}", e.getMessage());
				// nothing to do... who's next ?
			}

		} catch (Ex403Forbiden e) {
			return serveForbiden(request, e);
		} catch (Exception e) {
			return serve500(request, e);
		}

		return serve404(request);

	}

	private WorksResponse serveForbiden(WorksRequest request, Exception e) {
		WorksResponse response = new WorksResponse(HttpResponseStatus.FORBIDDEN);
		try {
			// TODO if dev show message if not dev dont show it
			response.html("<h1>forbidden 403</h1><br>" + e.getMessage());
		} catch (Ex500 e1) {
			l.error("{}", e1);
		}
		return response;
	}

	private WorksResponse serve500(WorksRequest request, Exception e) {
		WorksResponse response = new WorksResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		try {
			// TODO if dev show message if not dev dont show it
			response.html("<h1>500</h1><br>" + e.getMessage());
		} catch (Ex500 e1) {
			l.error("{}", e1);
		}
		return response;
	}

	public WorksResponse serve404(WorksRequest request) {

		WorksResponse response = new WorksResponse(HttpResponseStatus.NOT_FOUND);
		try {
			response.html("<h1>404</h1>");
		} catch (Ex500 e) {
			l.error("{}", e);
		}
		return response;

	}
}
