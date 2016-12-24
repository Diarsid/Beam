/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
public interface SmartConsoleCommandsCache {
    
    static SmartConsoleCommandsCache buildCache(
            IoInnerModule ioEngine, DataModule dataModule) {
        OperationsAnalizer operationsAnalizer = new OperationsAnalizer();
        OperationCandidatesResolver candidatesResolver = new OperationCandidatesResolver(ioEngine);
        ActionsResolver actionsResolver = new ActionsResolver(
                ioEngine, dataModule.getActionsChoiceDao());
        CommandsAnalizer commandsAnalizer = new CommandsAnalizer(
                actionsResolver, operationsAnalizer, candidatesResolver);
        
        return new SmartConsoleCommandsCacheWorker(
                ioEngine, commandsAnalizer, dataModule.getConsoleCommandsDao());
    }

    void addCommand(List<String> commandParams);

    void addCommand(String command);

    boolean deleteCached(String command);
    
    List<String> getConsoleCommandsOfPattern(String pattern);

    String getCommandByPattern(String command);
    
}
