package com.mirasworks.module.mvc;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.mirasworks.server.http.exceptions.Ex500;

public interface ItemplateEngine {

    public void configure();

    public void setTemplateDirectory(String path) throws IOException;

    public void setDefaultEncoding(String encoding);


    public void render(String templatePath,Writer outputstream, Map<String, Object> params) throws Ex500;
}
