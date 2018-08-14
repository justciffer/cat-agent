package com.eazytec.middleware.apm.advice;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.eazytec.middleware.apm.AgentDebug;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CatAroundAdvice implements AroundAdvice{
    public static final String D_CLIENT_ADDR = "X-CAT-CLIENT-ADDR";
    public static final String D_CLIENT_DOMAIN = "X-CAT-CLIENT-DOMAIN";
    public static final String D_CALL_TRACE_MODE = "X-CAT-TRACE-MODE";
    public static final String D_CALL_SERVER_ADDR = "X-CAT-SERVER-ADDR";
    public static final String D_CALL_SERVER_DOMAIN = "X-CAT-SERVER-DOMAIN";

    public static final String E_SERVER_DOMAIN = "server.domain";
    public static final String E_SERVER_ADDR = "server.address";

    public static final String E_CLIENT_DOMAIN = "client.domain";
    public static final String E_CLIENT_ADDR = "client.address";


    @Override
    public Object invoke(String originMethodName,Object obj, Method method, Object[] args) throws Throwable {

        String type = getTransactionType(originMethodName,obj,method,args) + " ";
        String name = getTransactionName(originMethodName,obj,method,args);
        if(name == null || name.length() == 0){
            //不开启监控
            return method.invoke(obj,args);
        }

        AgentDebug.info("invoke -> %s",originMethodName);

        Transaction t = Cat.newTransaction(type,name);
        Object o = null;
        try{
            try{
                doBefore(originMethodName,obj,method,args);
            }catch (Throwable ignore){}
            o = method.invoke(obj,args);
            t.setStatus(Transaction.SUCCESS);
        }catch (Exception e){
            t.setStatus(e);
            Cat.logError(e);
            throw new Exception(e);
        }finally {
            try{
                doAfter(originMethodName,obj,method,args,o);
            }catch (Throwable ignore){}
            t.complete();
        }
        return o;
    }

    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args){
        return method.getDeclaringClass().getSimpleName();
    }
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args){
        return originMethodName;
    }
    protected void doBefore(String originMethodName, Object obj, Method method, Object[] args){

    }
    protected void doAfter(String originMethodName, Object obj, Method method, Object[] args,Object res){
    }

    public static class RemoteContext implements Cat.Context {

        private Map<String, String> traceData = new HashMap<String, String>();

        @Override
        public void addProperty(String key, String value) {
            traceData.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return traceData.get(key);
        }

        public Map<String, String> getAllData() {
            return traceData;
        }

    }

    public static String getConcreteUri(String uri) {
        int index;
        if((index=uri.indexOf(";"))>-1){
            uri = uri.substring(0, index);
        }
        return uri;
    }

}
