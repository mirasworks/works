package com.mirasworks.module.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RendererJson implements Irenderer {
    private final Logger l = LoggerFactory.getLogger(RendererJson.class);

    public RendererJson() {

    }

    @Override
    public Object render(Object o) {

        if (o != null) {
            return o.toString();
        } else {
            l.warn("object to render is null");
            return "";
        }

    }
}
