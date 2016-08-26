/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.context.ExecutorContextWorker;
import diarsid.beam.core.modules.executor.os.OSProvider;
import diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor;
import diarsid.beam.core.modules.executor.os.search.FileSearcher;
import diarsid.beam.core.modules.executor.processors.workers.ProcessorsBuilderImpl;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.modules.executor.context.ExecutorContextWorker.createContext;
import static diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor.getExecutor;
import static diarsid.beam.core.modules.executor.os.search.FileSearcher.getSearcherWithDeepOf;

/**
 *
 * @author Diarsid
 */
class ExecutorModuleWorkerBuilder implements GemModuleBuilder<ExecutorModule> {
    
    private final DataModule dataModule;
    private final IoInnerModule ioInnerModule;
    private final ConfigModule configModule;
    
    ExecutorModuleWorkerBuilder(
            IoInnerModule io, 
            DataModule dataModule, 
            ConfigModule configModule) {
        
        this.dataModule = dataModule;
        this.ioInnerModule = io;
        this.configModule = configModule;
    }
    
    @Override
    public ExecutorModule buildModule() {
        FileSearcher fileSearcher = getSearcherWithDeepOf(2);
        SystemActionsExecutor actionsExecutor = getExecutor(this.ioInnerModule);
        
        SmartConsoleCommandsCache consoleCommandsCache = new SmartConsoleCommandsCache(
                this.ioInnerModule, this.dataModule.getConsoleCommandsDao());
        ExecutorContextWorker context = createContext(this.dataModule, this.ioInnerModule);
        OS os = OSProvider.getOS(
                this.ioInnerModule, 
                this.configModule, 
                actionsExecutor,
                fileSearcher,
                context); 
        ProcessorsBuilderImpl processorsBuilder = new ProcessorsBuilderImpl(
                this.ioInnerModule, 
                this.dataModule, 
                this.configModule, 
                context,
                os);         
        ExecutorModuleWorker actualExecutor = new ExecutorModuleWorker(
                this.ioInnerModule, 
                context, 
                processorsBuilder, 
                consoleCommandsCache);
        InvocationHandler preparedProxy = new ExecutorModuleProxy(
                actualExecutor, context);
        ExecutorModule proxyExecutor = (ExecutorModule) Proxy.newProxyInstance(
                ExecutorModule.class.getClassLoader(), 
                actualExecutor.getClass().getInterfaces(), 
                preparedProxy);
        return proxyExecutor;
    }
}
