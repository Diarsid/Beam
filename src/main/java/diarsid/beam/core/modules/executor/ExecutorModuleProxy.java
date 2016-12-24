/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import old.diarsid.beam.core.modules.ExecutorModule;

import diarsid.beam.core.modules.executor.context.ExecutorContextLifecycleController;
import diarsid.beam.core.util.Logs;

import static java.util.Objects.isNull;

import static diarsid.beam.core.modules.executor.ProxyMethodExecutionMode.INTERCEPT_AND_PROCEED;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleProxy implements InvocationHandler {
    
    private final ExecutorModule executorModule;
    private final ExecutorContextLifecycleController currentCommandContext;
    private final ExecutorModuleProxyArgumentsAnalizer argumentsAnalizer;
    
    ExecutorModuleProxy(
            ExecutorModule executorModule, 
            ExecutorContextLifecycleController currentCommandContext,
            ExecutorModuleProxyArgumentsAnalizer argumentsAnalizer) {        
        this.executorModule = executorModule;
        this.currentCommandContext = currentCommandContext;
        this.argumentsAnalizer = argumentsAnalizer;
    }
    
    @Override 
    public Object invoke(Object proxy, Method method, Object[] args) 
            throws Exception {  
        
        if ( isNull(args) ) {
            return this.justProceed(method, args);
        } else {
            if ( this.argumentsAnalizer.defineExecutionMode(args) == INTERCEPT_AND_PROCEED ) {
                return this.interceptCommandAndProceed(method, args);
            } else {
                return this.justProceed(method, args);
            }
        }
    }
    
    
    private Object interceptCommandAndProceed(Method method, Object[] args) 
            throws Exception {        
        Logs.debug("[EXECUTOR PROXY] method intercepted : "  + method.getName());
        Object invocationResult;
        this.currentCommandContext.createContextForCommand(
                this.extractCommandFromPassedArgs(args));
        invocationResult = method.invoke(this.executorModule, args);
        this.currentCommandContext.destroyCurrentContext();
        return invocationResult;
    }
    
    private List<String> extractCommandFromPassedArgs(Object[] args) {
        return (List<String>) args[0];
    }
    
    private Object justProceed(Method method, Object[] args) throws Exception {
        return method.invoke(this.executorModule, args);
    }
}
