package com.eazytec.middleware.apm.advice;

import java.lang.reflect.Method;

public interface AroundAdvice {
    Object invoke(String originMethodName,Object obj,Method method,Object[] args) throws Throwable;
}
