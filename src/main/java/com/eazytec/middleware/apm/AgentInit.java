package com.eazytec.middleware.apm;

import com.eazytec.middleware.apm.match.Execution;
import com.eazytec.middleware.apm.match.Pointcut;
import com.eazytec.middleware.apm.match.Regular;
import javassist.bytecode.AccessFlag;

import java.util.HashSet;
import java.util.Set;

public class AgentInit {

    public static final Regular regular = new Regular();

    public static final Set<String> excludePackages = new HashSet<>();
    public static final Set<String> excludeKeyWords = new HashSet<>();
    public static final Set<Integer> allowedMethodModifies = new HashSet<>();

    static {
        excludePackages.add("org.aspectj.");
        excludePackages.add("sun.reflect.");
        excludePackages.add("com.eazytec.middleware.apm.");
        excludePackages.add("javax.");
        excludePackages.add("java.");
        excludePackages.add("com.sun.");

        excludeKeyWords.add("$"); //TODO: 不支持内部类
        excludeKeyWords.add("__");
     /*   excludeKeyWords.add("$Proxy");
        excludeKeyWords.add("CGLIB$$");*/

        allowedMethodModifies.add(AccessFlag.PUBLIC);

        Pointcut controller =  Pointcut.create("spring-controller","com.eazytec.middleware.apm.advice.CatAroundAdvice")
                .execution(Execution.create("org.springframework.stereotype.Controller"));
        regular.add(controller);
        Pointcut service =  Pointcut.create("spring-service","com.eazytec.middleware.apm.advice.CatAroundAdvice")
                .execution(Execution.create("org.springframework.stereotype.Service"));
        regular.add(service);
        Pointcut dubboServer =  Pointcut.create("dubbo-server","com.eazytec.middleware.apm.advice.CatAroundAdvice")
                .execution(Execution.create("com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker","invoke",""));
        regular.add(dubboServer);
        Pointcut dubboClient =  Pointcut.create("dubbo-client","com.eazytec.middleware.apm.advice.CatAroundAdvice")
                .execution(Execution.create("com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker","invoke",""));
        regular.add(dubboClient);
    }

}
