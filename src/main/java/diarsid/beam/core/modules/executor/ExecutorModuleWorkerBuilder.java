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
import diarsid.beam.core.modules.executor.commandscache.SmartConsoleCommandsCache;
import diarsid.beam.core.modules.executor.context.ExecutorContextWorker;
import diarsid.beam.core.modules.executor.os.OSProvider;
import diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor;
import diarsid.beam.core.modules.executor.os.search.FileSearcher;
import diarsid.beam.core.modules.executor.processors.workers.ProcessorsBuilderImpl;
import diarsid.beam.shared.modules.ConfigModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.modules.executor.commandscache.SmartConsoleCommandsCache.buildCache;
import static diarsid.beam.core.modules.executor.context.ExecutorContextWorker.createContext;
import static diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor.getExecutor;
import static diarsid.beam.core.modules.executor.os.search.FileSearcher.getSearcherWithDepthsOf;

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
        FileSearcher fileSearcher = getSearcherWithDepthsOf(3, 3);
        PathAnalizer pathAnalizer = new PathAnalizer();
        SystemActionsExecutor actionsExecutor = getExecutor(this.ioInnerModule);
        
        SmartConsoleCommandsCache consoleCommandsCache = buildCache(
                this.ioInnerModule, this.dataModule);
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
                pathAnalizer,
                os);         
        ExecutorModuleWorker actualExecutor = new ExecutorModuleWorker(
                this.ioInnerModule, 
                context, 
                processorsBuilder, 
                consoleCommandsCache, 
                pathAnalizer);
        ExecutorModuleProxyArgumentsAnalizer analizer = 
                new ExecutorModuleProxyArgumentsAnalizer();
        InvocationHandler preparedProxy = new ExecutorModuleProxy(
                actualExecutor, context, analizer);
        ExecutorModule proxyExecutor = (ExecutorModule) Proxy.newProxyInstance(
                ExecutorModule.class.getClassLoader(), 
                actualExecutor.getClass().getInterfaces(), 
                preparedProxy);
        return proxyExecutor;
    }
}
