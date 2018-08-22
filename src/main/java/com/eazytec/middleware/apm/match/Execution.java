package com.eazytec.middleware.apm.match;

import com.eazytec.middleware.apm.AgentModifier;

import java.util.Arrays;
import java.util.List;

public class Execution {
    private String advice;
    private String annotation;
    private String interfaceName;
    private String className;
    private String methodName;
    private AgentModifier modifier;
    private List<String> paramTypes;

    public static Execution createByClassName(String className){
        return new Execution().className(className);
    }
    public static Execution createByAnnotation(String annotation){
        return new Execution().annotation(annotation);
    }

    public static Execution createByInterface(String interfaceName){
        return new Execution().interfaceName(interfaceName);
    }

    private Execution(){}

    private Execution(String annotation,String interfaceName,String className, String methodName, String paramTypesStr) {
        this.annotation = annotation;
        this.interfaceName = interfaceName;
        this.className = className;
        this.methodName = methodName;
        paramTypes(paramTypesStr);
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
    public Execution annotation(String annotation) {
        this.annotation = annotation;
        return this;
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
    public Execution className(String className) {
        this.className = className;
        return this;
    }
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public Execution methodName(String methodName) {
        this.methodName = methodName;
        return this;
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

    public Execution paramTypes(String paramTypesStr) {
        if(paramTypesStr != null && paramTypesStr.length() > 0){
            this.paramTypes = Arrays.asList(paramTypesStr.split(","));
        }
        return this;
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

    public String getInterfaceName() {
        return interfaceName;
    }
    public boolean hasInterfaceName() {
        return interfaceName != null && interfaceName.length() > 0;
    }
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    public Execution interfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public AgentModifier getModifier() {
        return modifier;
    }

    public void setModifier(AgentModifier modifier) {
        this.modifier = modifier;
    }
    public Execution modifier(AgentModifier modify) {
        this.modifier = modify;
        return this;
    }

    @Override
    public String toString() {
        return "Execution{" +
                "advice='" + advice + '\'' +
                ", annotation='" + annotation + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", modifier=" + modifier +
                ", paramTypes=" + paramTypes +
                '}';
    }
}
