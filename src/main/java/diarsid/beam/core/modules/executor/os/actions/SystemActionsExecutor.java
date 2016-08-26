/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.actions;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public interface SystemActionsExecutor {
    
    public static SystemActionsExecutor getExecutor(IoInnerModule ioEngine) {
        return new SystemActionsExecutorWorker(ioEngine);
    }

    void asyncExecuteWithRuntime(String command);

    void asyncBrowseWithDesktop(String url);

    void asyncOpenWithDesktop(String target);    
}
