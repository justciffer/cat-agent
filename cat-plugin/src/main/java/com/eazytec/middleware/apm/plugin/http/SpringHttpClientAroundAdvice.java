package com.eazytec.middleware.apm.plugin.http;

import com.dianping.cat.Cat;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;
import org.apache.http.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SpringHttpClientAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Call";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        ClientHttpRequest request = (ClientHttpRequest) obj;
        URI uri = request.getURI();
        return  "[http-" + request.getMethod().name()+"]" + uri.getScheme()+"://"+uri.getAuthority()+getConcreteUri(uri.getPath());
    }

    @Override
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){
        ClientHttpRequest request = (ClientHttpRequest) obj;

        request.getHeaders().add(D_CLIENT_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
        request.getHeaders().add(D_CLIENT_DOMAIN, Cat.getManager().getThreadLocalMessageTree().getDomain());
        request.getHeaders().add(D_CALL_TRACE_MODE, "trace");

        Cat.logEvent("Http.Method", request.getMethod().name());
        RemoteContext context = new RemoteContext();
        Cat.logRemoteCallClient(context);

        String messageId = context.getProperty(Cat.Context.CHILD);
        String rootId = context.getProperty(Cat.Context.ROOT);
        String parentId = context.getProperty(Cat.Context.PARENT);
        if (messageId != null) {
            request.getHeaders().add(D_TRACE_CHILD_ID,messageId);
        }
        if (parentId != null) {
            request.getHeaders().add(D_TRACE_PARENT_ID,parentId);
        }
        if (rootId != null) {
            request.getHeaders().add(D_TRACE_ROOT_ID,rootId);
        }
    /*    for (Map.Entry<String, String> entry : context.getAllData().entrySet()) {
            request.getHeaders().add(entry.getKey(),entry.getValue());
        }*/
    }

    @Override
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
        String addr = ((ClientHttpResponse)res).getHeaders().getFirst(D_CALL_SERVER_ADDR);
        String domain = ((ClientHttpResponse)res).getHeaders().getFirst(D_CALL_SERVER_DOMAIN);
        if(null != domain && domain.length() > 0){
            Cat.logEvent(E_SERVER_DOMAIN,domain);
        }
        if(null != addr && addr.length() > 0){
            Cat.logEvent(E_SERVER_ADDR,addr);
        }
    }

}
