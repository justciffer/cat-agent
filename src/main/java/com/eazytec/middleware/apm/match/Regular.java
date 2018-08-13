package com.eazytec.middleware.apm.match;



import javassist.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Regular extends ArrayList<Pointcut>{

    private Map<String,Execution> interfaces = new HashMap<>();

    private Map<String,Execution> annotations = new HashMap<>();
    private Map<String,Class> annotationsCache = new HashMap<>();

    private Map<String,List<Execution>> classNames = new HashMap<>();

    @Override
    public boolean add(Pointcut pointcut){
        super.add(pointcut);
        for(Execution execution : pointcut.getExecutions()){
            if(execution.hasInterfaceName()){
                interfaces.put(execution.getAnnotation(),execution);
            }
            else if(execution.hasAnnotation()){
                annotations.put(execution.getAnnotation(),execution);
            }
            else{
                if(execution.getClassName() == null || execution.getClassName().length() == 0 ){
                    continue;
                }
                List<Execution> executions = classNames.get(execution.getClassName());
                if(executions == null){
                    executions = new ArrayList<>();
                    classNames.put(execution.getClassName(),executions);
                }
                executions.add(execution);
            }
        }
        return true;
    }

    public List<Execution> matchClassName(CtClass ctClass, ClassLoader classLoader){
        if(classNames.keySet().size() > 0){
            for(String clsName : classNames.keySet()){
                if(ctClass.getName().startsWith(clsName)){
                    return classNames.get(clsName);
                }
            }
        }
        return null;
    }

    public Execution matchInterface(CtClass ctClass,ClassLoader classLoader){
        if(interfaces.keySet().size() > 0){
            CtClass[] clses = null;
            try {
                clses = ctClass.getInterfaces();
            } catch (NotFoundException ignore) {
                System.err.println("Regular matchInterface:" + ignore.getMessage());
            }

            if(clses != null){
                for(CtClass cls:clses){
                    if(interfaces.containsKey(cls.getName())){
                        return interfaces.get(cls.getName());
                    }
                }
            }
        }
        return null;
    }


    public Execution matchAnnotation(CtClass ctClass,ClassLoader classLoader){
        if(annotations.keySet().size() > 0){
             for(String anno : annotations.keySet()){
                 Class cls = annotationsCache.get(anno);
                 if(cls == null){
                     try {
                         cls = classLoader.loadClass(anno);
                         annotationsCache.put(anno,cls);
                     } catch (ClassNotFoundException e) {
                         System.err.println("Regular matchAnnotation:" +e.getMessage());
                     }
                 }
                 if(ctClass.hasAnnotation(cls)){
                     return annotations.get(anno);
                 }
             }
        }
        return null;
    }

    public boolean matchMethod(CtMethod ctMethod,Execution execution) {
        if(execution.getMethodName() == null || execution.getMethodName().length() == 0){
            return true;
        }
        if(!ctMethod.getName().startsWith(execution.getMethodName())){
            return false;
        }
        if(execution.getParamTypes() == null || execution.getParamTypes().size() == 0){
            return true;
        }
        CtClass[] typeCls = new CtClass[0];
        try {
            typeCls = ctMethod.getParameterTypes();
        } catch (NotFoundException e) {
            System.err.println("Regular matchMethod:" +e.getMessage());
        }
        if(typeCls.length != execution.getParamTypes().size()){
            return false;
        }
        for(int i=0;i<typeCls.length;i++ ){
            if(!typeCls[i].getName().equals(execution.getParamTypes().get(i))){
                return false;
            }
        }
        return true;
    }
}
