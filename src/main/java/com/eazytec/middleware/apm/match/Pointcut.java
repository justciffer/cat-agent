package com.eazytec.middleware.apm.match;

import java.util.ArrayList;
import java.util.List;

public class Pointcut {
    private String name;
    private List<Execution> executions;
    private String advice;

    private Pointcut(String name,String advice){
        this.name = name;
        this.advice = advice;
        executions = new ArrayList<>();
    }

    public static Pointcut create(String name,String advice){
        return new Pointcut(name,advice);
    }

    public String getName() {
        return name;
    }

    public Pointcut name(String name) {
        this.name = name;
        return this;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public Pointcut execution(Execution execution) {
        this.executions.add(execution);
        execution.setAdvice(this.advice);
        return this;
    }

    public String getAdvice() {
        return advice;
    }

    public Pointcut advice(String advice) {
        this.advice = advice;
        return this;
    }
}
