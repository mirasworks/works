package com.mirasworks.module.mvc;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.exceptions.Ex500;
import com.mirasworks.start.Application;

/**
 *
 * Damien MIRAS
 * This class is not singleton but should be instanciated once by the application
 * each instance has its own cache and setting
 *
 */
public class TemplateEngineBridge {
    private final Logger l = LoggerFactory.getLogger(TemplateEngineBridge.class);

    private  ItemplateEngine engine;
    //TODO configurable
    private  String templateExtension =   Application.getConfig().getKey("template.extension", ".html");

    private String templateDirectory = Application.getConfig().getKey("template.directory", "./public/template/");
    private String defaultEncoding = Application.getConfig().getKey("template.defaultEncoding", "UTF-8");

    public TemplateEngineBridge() {
        init();
      
    }

    public void init() {
        engine = new FreemarkerTemplateEngine();
        engine.configure();
        try {
            engine.setTemplateDirectory(templateDirectory);
        } catch (IOException e) {
            l.error(" wrong template directory " + e.getMessage());

        }
        engine.setDefaultEncoding(defaultEncoding);
    }

    public void render(String templateName, Writer outputstream, Map<String, Object> params) throws Ex500 {
        StringBuilder strb = new StringBuilder();
        strb.append(templateName);
        strb.append(templateExtension);
        engine.render(strb.toString(), outputstream, params);
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

}
