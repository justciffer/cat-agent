package com.eazytec.middleware.apm.proxy;


import com.eazytec.middleware.apm.AgentDebug;
import javassist.*;
import javassist.bytecode.AccessFlag;

import java.util.ArrayList;
import java.util.List;

public class MethodProxy {

    /**
     * 动态代理方法的方法名后缀
     */
    private final static String PROXY_CLASS_NAME_SUFFIX = "$CatProxy";

    /**
     * 保存被代理方法对象的变量名
     */
    private final static String PROXY_METHOD_VARIABLE = "_CatProxyMethod";

    private final static String AROUND_ASPECTJ_HANDLER_INTERFACE = "com.eazytec.middleware.apm.advice.AroundAdvice";


    private static final String  return_method_format =
            " {Object returnObject = null;\n" +
            "        %s advice = new %s();\n" +
            "        if (%s == null) {\n" +
            "            %s = Class.forName(\"%s\").getMethod(\"%s\", $sig);\n" +
            "        }\n" +
            "        returnObject = advice.invoke(%s,this, %s, $args);\n" +
            "        return ($r)returnObject;}";

    private static final String  void_method_format =
            "       { %s advice = new %s();\n" +
            "        if (%s == null) {\n" +
            "            %s = Class.forName(\"%s\").getMethod(\"%s\", $sig);\n" +
            "        }\n" +
            "        advice.invoke(%s,this, %s, $args);}";

    public static void aroundProxy(String targetClassName, CtClass targetClass, CtMethod targetMethod, String advice)
            throws NotFoundException, CannotCompileException {
        AgentDebug.info("create proxy -> %s",targetMethod.getLongName());

        String uniqueName = targetMethod.getName() + Math.abs(targetMethod.hashCode());

        //copy方法(不会复制注解)  %targetMethod%$CatProxy
        CtMethod newMethod = CtNewMethod.copy(targetMethod,targetClass,null);
        String newMethodName = uniqueName + PROXY_CLASS_NAME_SUFFIX;
        newMethod.setName(newMethodName);
        newMethod.setModifiers(AccessFlag.PUBLIC); //TODO: 必须设置成public
        targetClass.addMethod(newMethod);

        //创建变量存储method对象  %targetMethod_name%_CatProxyMethod
        String methodCache = uniqueName + PROXY_METHOD_VARIABLE;
        CtField ctField = CtField.make("private static java.lang.reflect.Method " + methodCache + " = null;", targetClass);
        targetClass.addField(ctField);

        //替换 targetMethod内容为代理函数
        String logName = targetClass.getSimpleName() + "." + getMethodLogName(targetMethod);
        String methodCode = getMethodBody(targetClassName,targetMethod,advice,newMethod.getName(),methodCache,logName);
//        System.out.println(">>> cat agent debug <<  [ code ] :   " + methodCode);
        targetMethod.setBody(methodCode);
    }

    private static String getMethodLogName(CtMethod targetMethod){
        StringBuilder stringBuilder = new StringBuilder(targetMethod.getName());
        stringBuilder.append("(");
        try {
            CtClass[] clses = targetMethod.getParameterTypes();
            for(int i = 0; i< clses.length ;i ++){
                stringBuilder.append(clses[i].getSimpleName());
                if(i != clses.length-1){
                    stringBuilder.append(",");
                }
            }
        } catch (NotFoundException ignore) {
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private static String getMethodBody(String targetClassName,CtMethod targetMethod,String advice,String newMethodName,String methodCache,String logName) throws NotFoundException {
        String formatStr = CtClass.voidType.getName().equals(targetMethod.getReturnType().getName()) ? void_method_format : return_method_format;
        List<String> formats = new ArrayList<>();
        formats.add(AROUND_ASPECTJ_HANDLER_INTERFACE);
        formats.add( advice);
        formats.add( methodCache);
        formats.add( methodCache);
        formats.add( targetClassName);
        formats.add( newMethodName);
        formats.add( "\""+logName+"\"");
        formats.add( methodCache);

        String code = String.format(formatStr,formats.toArray(new Object[]{}));
        return code;
    }

    private static String convert(String sign){
        return sign.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]", "");
    }

    private static void outAnnot(CtMethod m){
        try {
            System.out.println("------------" + m.getName() );
            for(Object o : m.getAnnotations()){
                System.out.println(o);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}