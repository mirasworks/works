package com.mirasworks.server.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WorksRequest extends DefaultHttpRequest {

    @SuppressWarnings("unused")
    private static Logger l = LoggerFactory.getLogger(WorksRequest.class);

    private String host;
    private String ipAdress;

    private Cookie cookie;



    private Map<String, List<String>> params;

    public WorksRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        super(httpVersion, method, uri);
        setHost(HttpHeaders.getHeader(this, Names.HOST, "unknown"));

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }



    @Override
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(super.toString());
        strBuff.append("host : ");
        strBuff.append(host);
        strBuff.append("\r\n\tipAdress : ");
        strBuff.append(ipAdress);
        strBuff.append("\r\n\tcookie : ");
        strBuff.append(cookie);

        strBuff.append("\r\n\turi :");
        strBuff.append(getUri());

        strBuff.append("\r\nheaders :");
        HttpHeaders headers = headers();
        if (headers != null) {
            for (Map.Entry<String, String> h : headers) {
                strBuff.append("\r\n\t ");
                strBuff.append(h.getKey());
                strBuff.append(" : ");
                strBuff.append(h.getValue());
            }
        }

        strBuff.append("\r\nparams :\r\n");
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    strBuff.append("\t");
                    strBuff.append(key);
                    strBuff.append(" : ");
                    strBuff.append(val);
                    strBuff.append("\r\n");
                }
            }
            strBuff.append("\r\n");
        }

        return strBuff.toString();
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public int getParamSize() {
        return params.size();
    }

    public boolean isEmptyParam() {
        return params.isEmpty();
    }

    public boolean paramContainsKey(Object key) {
        return params.containsKey(key);
    }

    public List<String> getParam(Object key) {
        return params.get(key);
    }

    public Set<String> getParamkeySet() {
        return params.keySet();
    }

    public Collection<List<String>> getParamValues() {
        return params.values();
    }

    public Set<Entry<String, List<String>>> getParamEntrySet() {
        return params.entrySet();
    }

}
