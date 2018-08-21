package com.eazytec.middleware.apm.plugin.http;

import com.dianping.cat.Cat;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;
import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpClientAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Call";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        HttpContext httpContext =  getHttpContext(args);
        URI uri = httpContext.getUri();
        return  "[http-" + httpContext.getHttpMethod()+"]" + uri.getScheme()+"://"+uri.getAuthority()+getConcreteUri(uri.getPath());
    }

    @Override
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){
        HttpContext httpContext =  getHttpContext(args);

        httpContext.getHttpMessage().setHeader(D_CLIENT_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
        httpContext.getHttpMessage().setHeader(D_CLIENT_DOMAIN, Cat.getManager().getThreadLocalMessageTree().getDomain());
        httpContext.getHttpMessage().setHeader(D_CALL_TRACE_MODE, "trace");

        Cat.logEvent("Http.Method", httpContext.getHttpMethod());
        RemoteContext context = new RemoteContext();
        Cat.logRemoteCallClient(context);

        for (Map.Entry<String, String> entry : context.getAllData().entrySet()) {
            httpContext.getHttpMessage().setHeader(entry.getKey(),entry.getValue());
        }
    }

    @Override
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
        Header addr = ((HttpResponse)res).getFirstHeader(D_CALL_SERVER_ADDR);
        Header domain = ((HttpResponse)res).getFirstHeader(D_CALL_SERVER_DOMAIN);
        if(null != domain){
            Cat.logEvent(E_SERVER_DOMAIN,domain.getValue());
        }
        if(null != addr){
            Cat.logEvent(E_SERVER_ADDR,addr.getValue());
        }
    }

    private static HttpContext getHttpContext(Object[] args) {
        HttpContext context = new HttpContext();

        HttpHost httpHost = null;
        HttpUriRequest uriRequest = null;
        HttpRequest request = null;
        for(Object param : args){
            if(param instanceof HttpHost){
                httpHost = (HttpHost)param;
            }
            if(param instanceof HttpUriRequest){
                uriRequest = (HttpUriRequest)param;
            }
            if(param instanceof HttpRequest){
                request = (HttpRequest)param;
            }
        }
        String httpMethod = "";
        URI uri = null;

        if(null != uriRequest){
            uri = uriRequest.getURI();
            httpMethod = uriRequest.getMethod();
            context.setHttpMessage(uriRequest);
            context.setUri(uri);
            context.setHttpMethod(httpMethod);
        }
        if(null != httpHost && null != request){
            try {
                uri = new URI(request.getRequestLine().getUri());
            } catch (URISyntaxException ignore) {
            }
            httpMethod = request.getRequestLine().getMethod();
            context.setHttpMessage(request);
            context.setUri(uri);
            context.setHttpMethod(httpMethod);
        }
        return context;
    }

    public static class HttpContext{
        private HttpMessage httpMessage = null;
        private String httpMethod = "";
        private URI uri = null;

        public HttpMessage getHttpMessage() {
            return httpMessage;
        }

        public void setHttpMessage(HttpMessage httpMessage) {
            this.httpMessage = httpMessage;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
    }

}
