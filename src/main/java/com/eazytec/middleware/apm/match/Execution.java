package com.eazytec.middleware.apm.match;

import java.util.Arrays;
import java.util.List;

public class Execution {


    private String advice;
    private String annotation;
    private String className;
    private String methodName;
    private List<String> paramTypes;

    public static Execution create(String className, String methodName, String paramTypesStr){
        return new Execution(null,className,methodName,paramTypesStr);
    }

    public static Execution create(String annotation){
        return new Execution(annotation,null,null,null);
    }


    private Execution(String annotation,String className, String methodName, String paramTypesStr) {
        this.annotation = annotation;
        this.className = className;
        this.methodName = methodName;
        if(paramTypesStr != null && paramTypesStr.length() > 0){
            paramTypes = Arrays.asList(paramTypesStr.split(","));
        }
    }

    public String getAnnotation() {
        return annotation;
    }
    public boolean hasAnnotation() {
        return annotation != null && annotation.length() > 0;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getClassName() {
        return className;
    }
    public boolean hasClassName() {
        return className != null && className.length() > 0;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean hasMethodName() {
        return methodName != null && methodName.length() > 0;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = paramTypes;
    }
    public boolean hasParamTypes() {
        return paramTypes != null && paramTypes.size() > 0;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
