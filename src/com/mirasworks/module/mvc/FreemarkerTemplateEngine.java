package com.mirasworks.module.mvc;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.server.http.exceptions.Ex500;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class FreemarkerTemplateEngine implements ItemplateEngine {

    private final Logger l = LoggerFactory.getLogger(FreemarkerTemplateEngine.class);

    private Configuration configuration = null;

    public FreemarkerTemplateEngine() {

    }

    //TODO rethrow a custom template exception format html if debug
    synchronized
    public void render(String templatePath,Writer outputstream, Map<String, Object> params) throws Ex500 {
        Template temp;

        //TASK https://github.com/mirasworks/works/issues/2 here watch the parse exp
        //open the template and inject the exception or maybe find a better way to 
        //inject some of flash message then retry to display the template
        //the template engine gives many option for exception handling 
        //and exception customization
        //read the doc there is differents  exption handling style for freemarker
        try {
            temp = configuration.getTemplate(templatePath);
            temp.process(params, outputstream);

        } catch (TemplateNotFoundException e) {
            l.error(e.getMessage());
            throw new Ex500(e.getCause());

        } catch (MalformedTemplateNameException e) {
            l.error(e.getMessage());
            throw new Ex500(e.getCause());

        } catch (ParseException e) {
            l.error(e.getMessage());
            throw new Ex500(e.getCause());

        } catch (IOException e) {
            l.error(e.getMessage());
            throw new Ex500(e.getCause());

        } catch (TemplateException e) {
            l.error(e.getMessage());
            throw new Ex500(e.getCause());

        }

    }

    public void configure() {
        // Create and adjust the configuration singleton
        configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


    }

    @Override
    public void setTemplateDirectory(String path) throws IOException {

        //TODO should be configurable
        configuration.setDirectoryForTemplateLoading(new File(path));

    }

    @Override
    public void setDefaultEncoding(String encoding) {
        configuration.setDefaultEncoding(encoding);

    }

}
