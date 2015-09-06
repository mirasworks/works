package com.mirasworks.server;

import org.jboss.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.module.mvc.TemplateEngineBridge;

public class Context {
    @SuppressWarnings("unused")
    private final Logger l = LoggerFactory.getLogger(Context.class);

    private SslContext sslCtx = null;
    private TemplateEngineBridge templateEngineBridge = null;

    public Context() {

    }


    public SslContext getSslCtx() {
        return sslCtx;
    }

    public void setSslCtx(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }




    public TemplateEngineBridge getTemplateEngineBridge() {
        return templateEngineBridge;
    }

    public void setTemplateEngineBridge(TemplateEngineBridge templateEngineBridge) {
        this.templateEngineBridge = templateEngineBridge;
    }

}
