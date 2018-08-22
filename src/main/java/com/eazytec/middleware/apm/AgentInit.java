package com.eazytec.middleware.apm;

import com.eazytec.middleware.apm.match.Execution;
import com.eazytec.middleware.apm.match.Pointcut;
import com.eazytec.middleware.apm.match.Regular;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgentInit {

    public static final Regular regular = new Regular();

    private static final Set<String> excludePackages = new HashSet<>();
    private static final Set<String> excludeKeyWords = new HashSet<>();

    private static final Map<String,String> modes = new HashMap<>();

    public static String pluginPath = null;


    private static boolean isLoaded = false;
    private static String mainClassLoader = null;

    static {
        excludePackages.add("org.aspectj.");
        excludePackages.add("sun.reflect.");
        excludePackages.add("com.eazytec.middleware.apm.");
        excludePackages.add("javax.");
        excludePackages.add("java.");
        excludePackages.add("com.sun.");
        excludePackages.add("com.alibaba.dubbo.common.bytecode.");

        excludeKeyWords.add("$"); //TODO: 不支持内部类
        excludeKeyWords.add("_");
        excludeKeyWords.add("__");

     /*   excludeKeyWords.add("$Proxy");
        excludeKeyWords.add("CGLIB$$");*/

        modes.put("springboot","org.springframework.boot.loader.LaunchedURLClassLoader");

        regular.add(
                Pointcut.create("spring-start","com.eazytec.middleware.apm.advice.CatAroundAdvice")
                        .execution(Execution.createByClassName("org.springframework.context.support.AbstractApplicationContext").methodName("finishRefresh")
                                .modifier(AgentModifier.PROTECTED))
        );

        regular.add(
                Pointcut.create("spring-rest-controller","com.eazytec.middleware.apm.plugin.spring.ControllerAroundAdvice")
                        .execution(Execution.createByAnnotation("org.springframework.web.bind.annotation.RestController").modifier(AgentModifier.PUBLIC))
        );
        regular.add(
                Pointcut.create("spring-controller","com.eazytec.middleware.apm.plugin.spring.ControllerAroundAdvice")
                        .execution(Execution.createByAnnotation("org.springframework.stereotype.Controller").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("spring-service","com.eazytec.middleware.apm.plugin.spring.ServiceAroundAdvice")
                        .execution(Execution.createByAnnotation("org.springframework.stereotype.Service").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("dubbo-client","com.eazytec.middleware.apm.plugin.dubbo.DubboClientAroundAdvice")
                        .execution(Execution.createByClassName("com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker").methodName("invoke").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("dubbo-server","com.eazytec.middleware.apm.plugin.dubbo.DubboServerAroundAdvice")
                        .execution(Execution.createByClassName("com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker").methodName("invoke").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("http-client","com.eazytec.middleware.apm.plugin.http.HttpClientAroundAdvice")
                        .execution(Execution.createByInterface("org.apache.http.client.HttpClient").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("spring-http-client","com.eazytec.middleware.apm.plugin.http.SpringHttpClientAroundAdvice")
                        .execution(Execution.createByInterface("org.springframework.http.client.ClientHttpRequest")
                                .methodName("execute").modifier(AgentModifier.PUBLIC))
        );

        regular.add(
                Pointcut.create("http-server","com.eazytec.middleware.apm.plugin.http.HttpServerAroundAdvice")
                        .execution(Execution.createByClassName("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter")
                                .methodName("handleInternal")
                                .paramTypes("javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse,org.springframework.web.method.HandlerMethod"))
        );

        regular.add(
                Pointcut.create("db-mysql","com.eazytec.middleware.apm.plugin.db.MysqlClientAroundAdvice")
                        .execution(Execution.createByClassName("com.mysql.jdbc.PreparedStatement").methodName("execute").modifier(AgentModifier.PUBLIC))
                        .execution(Execution.createByClassName("com.mysql.jdbc.PreparedStatement").methodName("executeQuery").modifier(AgentModifier.PUBLIC))
                        .execution(Execution.createByClassName("com.mysql.jdbc.PreparedStatement").methodName("executeUpdate").modifier(AgentModifier.PUBLIC))
        );

    }

    public static void init(String args){
        AgentDebug.open(args);
        // 初始化默认方法
        mainClassLoader = classLoader(args);
        AgentDebug.info("ClassLoader :" + mainClassLoader);
        pluginPath = getPluginPath();
        AgentDebug.info("plugin file :" + pluginPath);

        AgentDebug.info("config init");
    }

    private static String getPluginPath()
    {
        // 关键是这行...
        String path = AgentInit.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try
        {
            path = java.net.URLDecoder.decode(path, "UTF-8"); // 转换处理中文及空格
        }
        catch (java.io.UnsupportedEncodingException e)
        {  }
        return new File(path).getParent()+ File.separator + "cat-plugin.jar";
    }


    private static String classLoader(String args){
        if(args != null ){
            for(Map.Entry<String,String> entry : modes.entrySet()){
                if(args.contains(entry.getKey())){
                    return entry.getValue();
                }
            }
        }
        return Thread.currentThread().getContextClassLoader().getClass().getName();
    }

    //排除包路径
    public static boolean checkPackages(String className){
        if (excludePackages.isEmpty()) {
            return true;
        }
        for (String packageName : excludePackages) {
            if (className.startsWith(packageName)) {
                return false;
            }
        }
        return true;
    }

    //排除关键词
    public static boolean checkWords(String className){
        if (excludeKeyWords.isEmpty()) {
            return true;
        }
        for (String word : excludeKeyWords) {
            if (className.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public static void loadPluginJars(ClassLoader classLoader) {
        if (!isLoaded) {
            try {
                if (classLoader != null && classLoader instanceof URLClassLoader
                        && classLoader.getClass().getName().contains(mainClassLoader)) {
                    Method method = null;
                    try {
                        method = classLoader.getClass().getDeclaredMethod("addURL", new Class[]{URL.class});
                    } catch (Throwable t) {
                        method = classLoader.getClass().getSuperclass().getDeclaredMethod("addURL", new Class[]{URL.class});
                    }
                    if(method != null){
                        method.setAccessible(true);
                        File plugin = new File(AgentInit.pluginPath);
                        if(!plugin.exists()){
                            throw new Exception("no plugin jar find in :" + AgentInit.pluginPath);
                        }
                        method.invoke(classLoader, new Object[]{plugin.toURI().toURL()});
                        AgentDebug.info("classLoader init finish");
                        isLoaded = true;
                    }
                }

            } catch (Exception e) {
                System.err.println("init ClassLoader path error : " + e.getMessage());
            }
        }
    }

}
