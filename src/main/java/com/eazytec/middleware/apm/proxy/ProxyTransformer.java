package com.eazytec.middleware.apm.proxy;

import com.eazytec.middleware.apm.Agent;
import com.eazytec.middleware.apm.AgentInit;
import com.eazytec.middleware.apm.match.Execution;
import javassist.*;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.List;

public class ProxyTransformer implements ClassFileTransformer{

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(loader.getClass().getName().equals("sun.reflect.DelegatingClassLoader")){
            return classfileBuffer;
        }
        //加载插件
        AgentInit.loadPluginJars(loader);

        byte[] byteCode = classfileBuffer;
        className = className.replace('/', '.');

        //排除包路径
        if(!AgentInit.checkPackages(className)){
            return byteCode;
        }

        //排除关键词
        if(!AgentInit.checkWords(className)){
            return byteCode;
        }

        if (null == loader) {
            loader = Thread.currentThread().getContextClassLoader();
        }

        byteCode = matchClass(loader, className, byteCode);
        return byteCode;
    }

    public static CtClass getCLass(String className,ClassLoader loader){
        CtClass cc = null;
        ClassPool cp = ClassPool.getDefault();
        try {
            cc = cp.get(className);
        } catch (NotFoundException e) {
            cp.insertClassPath(new LoaderClassPath(loader));
            try {
                cc = cp.get(className);
            } catch (NotFoundException e1) {
                System.err.println("ProxyTransformer matchClass not found  class :" + className);
            }
        }
        return cc;
    }

    //match class
    private byte[] matchClass(ClassLoader loader, String className, byte[] byteCode) {
        CtClass cc = getCLass(className,loader);

        if (null == cc) {
            return byteCode;
        }

        if (!cc.isInterface()) {
            //TODO: 匹配CLASS 暂定之匹配一种，改成scope排序，范围最小的优先
            //接口
            Execution execution = AgentInit.regular.matchInterface(cc,loader);
            if(execution == null) {
                //注解
                execution = AgentInit.regular.matchAnnotation(cc,loader);
            }

            if(execution != null){
                byteCode = matchMethod(Collections.singletonList(execution),cc, className, byteCode);
            }
            else{
                //类名
                List<Execution> list = AgentInit.regular.matchClassName(cc,loader);
                if(list != null && list.size() > 0){
                    byteCode = matchMethod(list,cc, className, byteCode);
                }
            }
        }

        return byteCode;
    }

    //match method
    private byte[] matchMethod(List<Execution> executions ,CtClass cc, String className, byte[] byteCode) {
        try {
            CtMethod[] methods = cc.getDeclaredMethods();
            if (null != methods && methods.length > 0) {
                for (CtMethod m : methods) {
                    for (Execution execution : executions) {
                        if(AgentInit.regular.matchMethod(m,execution)){
                            MethodProxy.aroundProxy(className, cc, m, execution.getAdvice());
                            break;
                        }
                    }
                }
                byteCode = cc.toBytecode();
            }
            cc.detach();
        } catch (Exception e) {
            System.err.println("ProxyTransformer matchMethod:" +e.getMessage());
            e.printStackTrace();
        }
        return byteCode;
    }
}
