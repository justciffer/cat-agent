package com.eazytec.middleware.apm.advice;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import java.lang.reflect.Method;

public class CatAroundAdvice implements AroundAdvice{
    @Override
    public Object invoke(String originMethodName,Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("around :" + originMethodName );
        String type = getTransactionType(originMethodName,obj,method,args);
        String name = getTransactionName(originMethodName,obj,method,args);

        Transaction t = Cat.newTransaction(type,name);
        try{
            Object o =  method.invoke(obj,args);
            t.setStatus(Transaction.SUCCESS);
            return o;
        }catch (Exception e){
            t.setStatus(e);
            throw new Exception(e);
        }finally {
            t.complete();
        }
    }

    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args){
        return method.getDeclaringClass().getSimpleName();
    }
    protected String getTransactionName(String originMethodName, Object obj, Method method, Object[] args){
        return originMethodName;
    }

}
