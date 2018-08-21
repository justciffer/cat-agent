package com.eazytec.middleware.apm.plugin.spring;

import com.eazytec.middleware.apm.advice.CatAroundAdvice;

import java.lang.reflect.Method;

public class ControllerAroundAdvice extends CatAroundAdvice {

    @Override
    protected String getTransactionType(String originMethodName, Object obj, Method method, Object[] args) {
        return "URL";
    }

}
