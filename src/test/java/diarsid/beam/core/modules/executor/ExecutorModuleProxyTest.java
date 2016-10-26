/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.executor.context.ExecutorContextLifecycleController;

import static org.mockito.Mockito.*;

/**
 *
 * @author Diarsid
 */
public class ExecutorModuleProxyTest {
    
    private final ExecutorModule executor;
    private final ExecutorContextLifecycleController commandContext;
    private final Object proxyVoidTarget;
    
    private final List<String> listOfStrings;
    private final Object[] args_listOfStrings;
    private final Object[] args_boolean;
    private final Object[] args_string;
    {
        executor = mock(ExecutorModule.class);
        commandContext = mock(ExecutorContextLifecycleController.class);
        proxyVoidTarget = new Object();        
        listOfStrings = new ArrayList<>();
        listOfStrings.add("open");
        listOfStrings.add("j");
        listOfStrings.add("in");
        listOfStrings.add("eng");
        args_listOfStrings = new Object[] {listOfStrings};
        args_boolean = new Object[] {true};
        args_string = new Object[] {"location"};
    }
    
    private final ExecutorModuleProxy testedProxy;
    {
        ExecutorModuleProxyArgumentsAnalizer analizer =
                new ExecutorModuleProxyArgumentsAnalizer();
        testedProxy = new ExecutorModuleProxy(
                executor, commandContext, analizer);
    }
    
    private Method methodAcceptingListOfStrings;
    private Method methodAcceptingBoolean;
    private Method methodAcceptingString;
    
    @Before
    public void init() throws Exception {
        methodAcceptingListOfStrings = ExecutorModule.class
                .getMethod("open", List.class);
        methodAcceptingBoolean = ExecutorModule.class
                .getMethod("setIntelligentActive", boolean.class);
        methodAcceptingString = ExecutorModule.class
                .getMethod("deleteBatch", String.class);
    }

    /**
     * Test of invoke method, of class ExecutorModuleProxy.
     */
    @Test
    public void testInvoke_boolean() throws Exception {
        testedProxy.invoke(proxyVoidTarget, methodAcceptingBoolean, args_boolean);
        verify(commandContext, never()).createContextForCommand(listOfStrings);
        verify(commandContext, never()).destroyCurrentContext();
    }
    
    /**
     * Test of invoke method, of class ExecutorModuleProxy.
     */
    @Test
    public void testInvoke_string() throws Exception {
        testedProxy.invoke(proxyVoidTarget, methodAcceptingString, args_string);
        verify(commandContext, never()).createContextForCommand(listOfStrings);
        verify(commandContext, never()).destroyCurrentContext();
    }
    
    /**
     * Test of invoke method, of class ExecutorModuleProxy.
     */
    @Test
    public void testInvoke_listOfStrings() throws Exception {
        testedProxy.invoke(
                proxyVoidTarget, 
                methodAcceptingListOfStrings, 
                args_listOfStrings);
        verify(commandContext).createContextForCommand(listOfStrings);
        verify(commandContext).destroyCurrentContext();
    }
}