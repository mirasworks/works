package com.mirasworks.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.module.mvc.Controller;
import com.mirasworks.module.mvc.TemplateEngineBridge;
import com.mirasworks.server.http.WorksRequest;
import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.Ex403Forbiden;
import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.server.http.exceptions.ExHttp;
import com.mirasworks.server.http.exceptions.ExNotMe;

public class ControllerInvokerModule implements Imodule {
	private final Logger l = LoggerFactory.getLogger(ControllerInvokerModule.class);
	private static String controllerPackageName = "controller.";
	private static String controllerSuffix = "Controller";

	private StringBuffer classPathStrb = null;
	private TemplateEngineBridge templateEngine;

	public ControllerInvokerModule(Context context) {

		this.templateEngine = context.getTemplateEngineBridge();
		classPathStrb = new StringBuffer();
		classPathStrb.append(controllerPackageName);

	}

	public WorksResponse serve(WorksRequest request) throws ExHttp {

		String uri = request.getUri();
		if (uri != null && uri.contains(".")) {
			throw new ExNotMe("route contains dots");
		}

		Route route = new Route(request);

		classPathStrb.append(route.getControllerName());
		classPathStrb.append(controllerSuffix);
		String classPath = classPathStrb.toString();

		String methodName = route.getMethodName();

		Class<?> clazz;
		Constructor<?> ctor;


		try {
			clazz = Class.forName(classPath);
		} catch (ClassNotFoundException e) {
			l.error("class not found : " + e.getMessage());
			throw new ExNotMe(e);
		}

		try {
			ctor = clazz.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			l.error("constructor  not found : " + e.getMessage());
			throw new ExNotMe(e);
		}

		Object object;
		try {
			object = ctor.newInstance();

		} catch (InstantiationException e) {
			l.error("instanciation failed : " + e.getMessage());
			throw new ExNotMe(e);

		} catch (IllegalAccessException e) {
			l.error("illegal access exp : " + e.getMessage());
			throw new ExNotMe(e);

		} catch (IllegalArgumentException e) {
			l.error("illegalArgument " + e.getMessage());
			throw new ExNotMe(e);

		} catch (InvocationTargetException e) {
			l.error("invocationTarget exp ( exception caused by the controller )" + e.getCause().getMessage());
			throw new ExNotMe(e.getCause());

		}

		if (object instanceof Controller) {

			Method method = null;
			Controller controller = null;
			controller = (Controller) object;
			StringBuilder strb = new StringBuilder();
			strb.append(route.getControllerName());
			strb.append("/");
			strb.append(methodName);

			controller.setTemplatePath(strb.toString());
			controller.setTemplateEngine(templateEngine);

			try {
				controller.setRequest(request);
				method = object.getClass().getMethod(methodName, new Class[] {});
				// here inject the params
				Object[] params = new Object[] {};
				method.invoke(controller, params);

			} catch (SecurityException e) {
				l.warn(e.getMessage());
				throw new Ex403Forbiden(e);

			} catch (NoSuchMethodException e) {
				l.warn(e.getMessage());
				throw new ExNotMe(e);

			} catch (IllegalAccessException e) {
				l.info(e.getMessage());
				throw new ExNotMe(e);

			} catch (IllegalArgumentException e) {
				l.info(e.getMessage());
				throw new ExNotMe(e);

			} catch (InvocationTargetException e) {
				//something get wrong in the client controller code
				l.error(e.getMessage());
				throw new Ex500(e);

			}

			WorksResponse response = controller.getResponse();
			return response;

		} else {
			throw new ExNotMe("not a controller instance");
		}

	}

}
