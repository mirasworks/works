package com.mirasworks.http;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLException;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirasworks.module.mvc.TemplateEngineBridge;
import com.mirasworks.server.AbstractServer;
import com.mirasworks.server.Context;
import com.mirasworks.start.Application;
//
public class Server extends AbstractServer {

    private final Logger l = LoggerFactory.getLogger(Server.class);
    // Configure SSL.
    private SslContext   sslCtx;

    @Override
    public void start() {

        super.start();


        Context context = new Context();
        context.setTemplateEngineBridge(new TemplateEngineBridge());

        if ("true".equals(Application.getConfig().getKey("http.ssl","65635"))) {
            SelfSignedCertificate ssc;
            try {
                ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } catch (SSLException e) {
                l.error(e.getMessage());
            } catch (CertificateException e) {
                l.error(e.getMessage());
            }
        } else {
            sslCtx = null;
        }

        context.setSslCtx(sslCtx);

        // Configure the server.
        NioServerSocketChannelFactory factory = null;
        factory = new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
        ServerBootstrap serverBosstrap = new ServerBootstrap(factory);



        // Set up the event pipeline factory.
        try {
            serverBosstrap.setOption("child.tcpNodelay", true);
            serverBosstrap.setPipelineFactory(new PipelineFactory(context));

            // Bind and start to accept incoming connections.
            serverBosstrap.bind(new InetSocketAddress(getPort()));
            l.info("{} server started listenning at {} port", getSeverTypeName(), getPort());
            l.info("Use url http://localhost:{}/", getPort());

        } catch (ChannelException e) {
            l.error("OhO seem it could not bind \r\n", e);
        } catch (Exception e) {
            l.error("OHO\r\n", e);
        }

    }


    @Override
    public String getSeverTypeName() {

        return "Http";
    }

    @Override
    public int getDefaultPort() {
        return 8084;
    }

}
