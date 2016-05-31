/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import diarsid.beam.core.modules.ExecutorModule;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleIntelligentProxy implements InvocationHandler {
    
    private final ExecutorModule executorModule;
    private final CurrentlyExecutedCommandContext currentCommandContext;
    
    ExecutorModuleIntelligentProxy(
            ExecutorModule executorModule, 
            CurrentlyExecutedCommandContext currentCommandContext) {        
        this.executorModule = executorModule;
        this.currentCommandContext = currentCommandContext;
    }
    
    @Override 
    public Object invoke(Object proxy, Method method, Object[] args) 
            throws Exception {  
        
        if ( this.ifPassedArgsIsParseableCommand(args) ) {
            return this.interceptCommandAndProceed(method, args);
        } else {
            return this.justProceed(method, args);
        }
    }
    
    private Object interceptCommandAndProceed(Method method, Object[] args) 
            throws Exception {
        
        System.out.println("[PROXY] method intercepted : "  + method.getName());
        Object invocationResult;
        this.currentCommandContext.beginCurrentCommandState(
                this.extractCommandFromPassedArgs(args));
        invocationResult = method.invoke(this.executorModule, args);
        this.currentCommandContext.destroyCurrentCommandState();
        return invocationResult;
    }
    
    private List<String> extractCommandFromPassedArgs(Object[] args) {
        return (List<String>) args[0];
    }
    
    private Object justProceed(Method method, Object[] args) throws Exception {
        return method.invoke(this.executorModule, args);
    }
    
    private boolean ifPassedArgsIsParseableCommand(Object[] args) {
        if ( (args == null) || (args.length != 1) ) {
            return false;
        } else {
            return this.ifSingleArgumentIsListOfStrings(args[0]);
        }
    }
    
    private boolean ifSingleArgumentIsListOfStrings(Object argument) {
        if ( argument instanceof List<?> ) {
            return this.ifListContainsStrings( (List<Object>) argument );
        } else {
            return false;
        }
    }
    
    private boolean ifListContainsStrings(List<Object> list) {
        // if this argument is real command, passed in from an external
        // ExecutorModule user, List<String> SHOULD have elements in it. 
        // If otherwise, this means that external usage of ExecutorModule
        // is invalid and actual ExecutorModule method should not be 
        // invoked at all as such invocation makes no sense.
        if ( list.isEmpty() ) {
            return false;
        } else {
            return ( (list.get(0)) instanceof String );
        }
    }
}
