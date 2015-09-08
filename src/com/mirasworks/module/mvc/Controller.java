package com.mirasworks.module.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {
    @SuppressWarnings("unused")
    private final Logger l = LoggerFactory.getLogger(Controller.class);

    String content = "";
    protected WorksResponse response = null;
    private String templatePath = null;
    private Map<String, Object> view = new HashMap<String, Object>();
    private TemplateEngineBridge templateEngine;

    public Controller() {
        response = new WorksResponse();
    }

    public void setTemplateEngine(TemplateEngineBridge templateEngine) {
        this.templateEngine = templateEngine;
    }

    public Object addViewParam(String key, Object value) {
        return view.put(key, value);
    }

    /**
     * It is the template path and name without the extension the extension is
     * append after this call. extension is configurable
     *
     * @param templatePath
     */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }



    public WorksResponse getResponse() {


        // TODO monitor the template's changes, if it changes it must be
        // flushed from the cache and reloaded
        if (templatePath != null && templateEngine != null) {
            StringWriter stringWriter = new StringWriter();
            templateEngine.render(templatePath, stringWriter, view);
            String html = stringWriter.toString();
            stringWriter.flush();
            try {
                response.out = new ByteArrayOutputStream();
                //here the charset must be send by the page
                //and must be validated
                response.out.write(html.getBytes( "UTF-8"));

            } catch (UnsupportedEncodingException e) {
            	// TODO throw it back nicely 
                e.printStackTrace();

            } catch (IOException e) {
                // TODO throw it back nicely 
                e.printStackTrace();
            }

        }

        return response;
    }

}
