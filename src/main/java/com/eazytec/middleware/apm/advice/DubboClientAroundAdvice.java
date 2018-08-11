package com.eazytec.middleware.apm.advice;

import com.alibaba.dubbo.rpc.RpcInvocation;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import java.lang.reflect.Method;

public class DubboClientAroundAdvice extends CatAroundAdvice{

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "Call.Dubbo.Client";
    }

    @Override
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args) {
        if(args.length > 0 && args[0] != null){
            try{
                RpcInvocation rpcInvocation =  (RpcInvocation)args[0];
                return rpcInvocation.getInvoker().getInterface().getSimpleName()+"."+rpcInvocation.getMethodName();
            }catch (Exception ignore){  }
        }
        return "unknown";
    }
}
