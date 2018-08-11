package com.eazytec.middleware.apm.plugin.http;

import com.dianping.cat.Cat;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class HttpServerAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Receive.HttpRequest";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        HttpServletRequest request = (HttpServletRequest)args[0];
        return  "[" + request.getMethod()+"]" + getConcreteUri(request.getRequestURI());
    }

    @Override
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){
        HttpServletRequest request = (HttpServletRequest)args[0];

        RemoteContext context = new RemoteContext();
        context.addProperty(Cat.Context.CHILD, request.getHeader(Cat.Context.CHILD));
        context.addProperty(Cat.Context.PARENT, request.getHeader(Cat.Context.PARENT));
        context.addProperty(Cat.Context.ROOT, request.getHeader(Cat.Context.ROOT));
        Cat.logRemoteCallServer(context);

        Cat.logEvent(E_CLIENT_ADDR,request.getHeader(D_CLIENT_ADDR));
        Cat.logEvent(E_CLIENT_DOMAIN,request.getHeader(D_CLIENT_DOMAIN));

        HttpServletResponse response = (HttpServletResponse)args[1];
        response.setHeader(D_CALL_SERVER_DOMAIN, Cat.getManager().getDomain());
        response.setHeader(D_CALL_SERVER_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
    }

    @Override
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
    }

}
