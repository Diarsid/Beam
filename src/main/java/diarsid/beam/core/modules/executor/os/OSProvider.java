/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor;
import diarsid.beam.core.modules.executor.os.search.FileSearcher;
import diarsid.beam.shared.modules.ConfigModule;

/**
 *
 * @author Diarsid
 */
public interface OSProvider {
    
    static OS getOS(
            IoInnerModule io, 
            ConfigModule config, 
            SystemActionsExecutor actionsExecutor,
            FileSearcher fileSearcher,
            IntelligentExecutorCommandContext intelligentContext) {
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")) {
            return new OSWorker(
                    io, config, actionsExecutor, fileSearcher, intelligentContext);
        } else if (systemName.contains("x")) {
            // Program does not have OSUnix implementation for working under this OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Program does not have OS *nix implementation yet.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        } else {
            // Some error occured or there is unknown OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Unsupported or unknown OS.", 
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }     
    }
}
