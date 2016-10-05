/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public interface SmartConsoleCommandsCache {
    
    static SmartConsoleCommandsCache buildCache(
            IoInnerModule ioEngine, DataModule dataModule) {
        ActionsResolver actionsResolver = new ActionsResolver(
                ioEngine, dataModule.getActionsChoiceDao());
        return new SmartConsoleCommandsCacheWorker(
                ioEngine, actionsResolver, dataModule.getConsoleCommandsDao());
    }

    void addCommand(List<String> commandParams);

    void addCommand(String command);

    boolean deleteCached(String command);
    
    List<String> getConsoleCommandsOfPattern(String pattern);

    String getPatternCommandForExecution(String command);
    
}
