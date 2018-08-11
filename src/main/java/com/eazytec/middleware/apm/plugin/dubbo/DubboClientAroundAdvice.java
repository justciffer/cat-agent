package com.eazytec.middleware.apm.plugin.dubbo;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.dianping.cat.Cat;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DubboClientAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Consume.Dubbo.Service";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        if(args.length > 0 && args[0] != null){
            try{
                RpcInvocation rpcInvocation =  (RpcInvocation)args[0];

                if(null == rpcInvocation.getInvoker()){
                    rpcInvocation.setInvoker((Invoker<?>) obj);
                }
                return rpcInvocation.getInvoker().getInterface().getSimpleName()+"."+ rpcInvocation.getMethodName();
            }catch (Exception ignore){
            }
        }
        return "unknown";
    }

    @Override
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){
        if(args.length > 0 && args[0] != null) {
            RpcInvocation rpcInvocation = (RpcInvocation) args[0];
            rpcInvocation.setAttachment(D_CLIENT_ADDR, Cat.getManager().getThreadLocalMessageTree().getIpAddress());
            rpcInvocation.setAttachment(D_CLIENT_DOMAIN, Cat.getManager().getThreadLocalMessageTree().getDomain());
            rpcInvocation.setAttachment(D_CALL_TRACE_MODE, "trace");

            RemoteContext context = new RemoteContext();
            Cat.logRemoteCallClient(context);

            for (Map.Entry<String, String> entry : context.getAllData().entrySet()) {
                rpcInvocation.setAttachment(entry.getKey(),entry.getValue());
            }
        }
    }

    @Override
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
        Result result = ((Result)res);
        String serverDomain = result.getAttachment(D_CALL_SERVER_DOMAIN);
        String serverAddr = result.getAttachment(D_CALL_SERVER_ADDR);
        if(null != serverDomain && serverDomain.length() > 0){
            Cat.logEvent(E_SERVER_DOMAIN,serverDomain);
        }
        if(null != serverAddr && serverAddr.length() > 0){
            Cat.logEvent(E_SERVER_ADDR,serverAddr);
        }
    }

}
