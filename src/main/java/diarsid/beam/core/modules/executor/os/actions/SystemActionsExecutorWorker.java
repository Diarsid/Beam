/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.actions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
class SystemActionsExecutorWorker implements SystemActionsExecutor {        
    
    private final IoInnerModule ioEngine;
    private final ExecutorService executorService;
    
    SystemActionsExecutorWorker(IoInnerModule ioEngine) {
        this.ioEngine = ioEngine;
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    @Override
    public void asyncOpenWithDesktop(String target) {
        this.executeAsync(new RunnableDesktopOpenAction(this.ioEngine, target));
    }
    
    @Override
    public void asyncExecuteWithRuntime(String command) {
        this.executeAsync(new RunnableRuntimeCommandAction(this.ioEngine, command));
    }
    
    @Override
    public void asyncOpenWithBrowser(String url) {
        this.executeAsync(new RunnableBrowserAction(this.ioEngine, url));
    }
    
    private void executeAsync(AbstractRunnableAction task) {
        this.executorService.execute(task);
    }
}
