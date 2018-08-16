package com.eazytec.middleware.apm;

import com.eazytec.middleware.apm.proxy.ProxyTransformer;
import javassist.runtime.Desc;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class Agent {

    private static ClassFileTransformer s_transformer = new ProxyTransformer();
    /**
     * agentArs : debug|springboot
     */
    public static void premain(String agentArs, Instrumentation inst)  throws IOException {
        AgentInit.init(agentArs);
        Desc.useContextClassLoader = true;
        inst.addTransformer(s_transformer);
    }

}
