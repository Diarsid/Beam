/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Diarsid
 */
class CurrentlyExecutedCommandIntelligentContext 
        implements CurrentlyExecutedCommandHolder, 
                   CurrentlyExecutedCommandContainer {
    
    private final ThreadLocal<List<String>> currentCommand;
    
    CurrentlyExecutedCommandIntelligentContext() {
        this.currentCommand = new ThreadLocal<>();
    }
    
    @Override
    public void saveCurrentlyExecutedCommand(List<String> commandParams) {
        System.out.println("[EXECUTOR PROXY] command intercepted!");
        System.out.println("[EXECUTOR PROXY] " + commandParams.toString());
        this.currentCommand.set(commandParams);
    }
    
    @Override
    public void clearCurrentlyExecutedCommand() {
        System.out.println("[EXECUTOR PROXY] end of interception...");
        System.out.println("[EXECUTOR PROXY] " + currentCommand.get().toString());
        System.out.println();
        this.currentCommand.remove();
    }
    
    @Override
    public List<String> getCurrentlyExecutedCommand() {
        return this.currentCommand.get();
    }
    
    @Override
    public void adjustCurrentlyExecutedCommand(String... newCommand) {
        this.currentCommand.set(Arrays.asList(newCommand));
    }
}
