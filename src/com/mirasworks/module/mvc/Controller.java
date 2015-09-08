package com.mirasworks.module.mvc;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.WorksResponse;
import com.mirasworks.server.http.exceptions.Ex500;

public class Controller {
	@SuppressWarnings("unused")
	private final Logger l = LoggerFactory.getLogger(Controller.class);

	private String templatePath = null;
	private Map<String, Object> view = new HashMap<String, Object>();
	private TemplateEngineBridge templateEngine;

	private String html = "";
	private String text = "";

	private static final int RENDER_TEXT = 0;
	private static final int RENDER_HTML = 2;
	private static final int RENDER_TEMPLATE = 3;

	private int renderMode = RENDER_TEMPLATE;;

	public Controller() {

	}

	public final void setTemplateEngine(TemplateEngineBridge templateEngine) {
		this.templateEngine = templateEngine;
	}

	public final Object addViewParam(String key, Object value) {
		renderMode = RENDER_TEMPLATE;
		return view.put(key, value);
	}

	public void setRenderMode(int renderMode) {
		// user should not switch render mode unless he knows what he does
		// the controller is request scoped so it mean it has changes inside a
		// method
		// TODO if the controller is session scoped or application scoped
		// detect the caller method in dev mode and do the test in devmode only
		if (this.renderMode != renderMode && renderMode != RENDER_TEMPLATE) {
			// TODO create enum for user friendly message
			l.info("render mode should not changes");
		}
		this.renderMode = renderMode;
	}

	/**
	 * It is the template path and name without the extension the extension is
	 * append after this call. extension is configurable
	 *
	 * @param templatePath
	 */
	public final void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public final WorksResponse getResponse() throws Ex500 {

		WorksResponse response = new WorksResponse();
		switch (renderMode) {
			case RENDER_TEMPLATE:
				if (templatePath != null) {
					if (templateEngine != null) {
						StringWriter stringWriter = new StringWriter();
						templateEngine.render(templatePath, stringWriter, view);
						String html = stringWriter.toString();
						stringWriter.flush();
						response.html(html);
					} else {
						throw new Ex500("No template engine ");
					}

				} else {
					throw new Ex500("No template path ");
				}
				break;

			case RENDER_HTML:
				response.html(getHtml());
				break;
			case RENDER_TEXT:
				response.text(getText());
				break;

			default:
				throw new Ex500("No render method used ");

		}

		response.setStatus(HttpResponseStatus.OK);
		return response;
	}

	public final String getHtml() {
		return html;
	}

	public final void setHtml(String html) {
		setRenderMode(RENDER_HTML);
		this.html = html;
	}

	public final String getText() {
		return text;
	}

	public final void setText(String text) {
		setRenderMode(RENDER_TEXT);
		this.text = text;
	}

}
