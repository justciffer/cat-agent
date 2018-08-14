package com.eazytec.middleware.apm;


public class AgentDebug {
    public static final String DEBUG = "debug";
    private static boolean debug = false;

    private final static String info = "---------------------------- [ cat agent ] : %s";

    public static void open(String arg) {
        if(AgentDebug.DEBUG.equals(arg)){
            debug = true;
            AgentDebug.info("open debug mode!");
        }
    }

    public static void open() {
        debug = true;
    }

    public static void close(){
        debug = false;
    }

    public static void info(String f, String...args)
    {
        if(debug)
        {
            System.out.println(String.format(info, String.format(f,args)));
        }
    }

    public static void info(String f)
    {
        if(debug)
        {
            System.out.println(String.format(info,f));
        }
    }
}
