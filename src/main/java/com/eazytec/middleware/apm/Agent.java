package com.eazytec.middleware.apm;

import com.eazytec.middleware.apm.proxy.ProxyTransformer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String agentArs, Instrumentation inst)  throws IOException {
        //TODO: 配置文件 agentArs
        inst.addTransformer(new ProxyTransformer());
    }

}
