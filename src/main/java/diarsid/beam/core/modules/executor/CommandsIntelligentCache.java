/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;

/**
 *
 * @author Diarsid
 */
class CommandsIntelligentCache {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorConsoleCommands dao;
        
    CommandsIntelligentCache(
            IoInnerModule ioEngine,
            DaoExecutorConsoleCommands consoleCommandsDao) {  
        
        this.ioEngine = ioEngine;
        this.dao = consoleCommandsDao;
    }    
    
    void addCommand(List<String> commandParams) {
        this.dao.saveNewConsoleCommand(String.join(" ", commandParams));
    }
    
    void addCommand(String command) {
        this.dao.saveNewConsoleCommand(command);
    }
    
    String getPatternCommandForExecution(String command) {
        return this.intelligentSearchInCache(command);
    }    
    
    private boolean ifCommandActionsAreEqual(String previous, String choosed) {
        return ( previous.charAt(0) == choosed.charAt(0) );
    }
    
    private String intelligentSearchInCache(String pattern) {        
        
        if (pattern.length() < 2) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        }
        Set<String> commandsCache = 
                this.dao.getImprovedCommandsForPattern(pattern);
        if ( commandsCache.isEmpty() ) {
            return "";
        }
        System.out.println("[COMM CACHE DEBUG] raw commands: " + commandsCache);
        Map<Character, String> chosenCommands = new HashMap<>();
        char operationToken;
        for (String testedCommand : commandsCache) {
            operationToken = testedCommand.charAt(0);
            if (chosenCommands.containsKey(operationToken)) {
                if (testedCommand.length() < chosenCommands.get(operationToken).length()) {
                    chosenCommands.put(operationToken, testedCommand);
                }
            } else {
                chosenCommands.put(operationToken, testedCommand);
            }
        }
        
        System.out.println("[COMM CACHE DEBUG] chosen commands: " + chosenCommands);
        
        if ( chosenCommands.size() == 1 ) {
            return this.refineCommandFromUnnecessaryParts(
                    new ArrayList<>(chosenCommands.values()).get(0));
        } else {
            return this.askUserWhichActionToPerform(
                    new ArrayList<>(chosenCommands.values()));
        }
    }

    private String refineCommandFromUnnecessaryParts(
                String chosenPreviousCommand) {
        
        String[] parts = chosenPreviousCommand.split("\\s+");
        StringJoiner futureCommand = new StringJoiner(" ");
        if ( parts.length > 1 ) {
            futureCommand.add(parts[0]).add(parts[1]);
            if ( (parts.length > 3) && (parts[2].equals("in")) ) {            
                futureCommand.add(parts[2]).add(parts[3]);
            }
            return futureCommand.toString();
        } else {
            return "";
        }
    }

    private String askUserWhichActionToPerform(List<String> chosenCommands) {
        int chosen = this.ioEngine.resolveVariantsWithExternalIO(
                "action?", chosenCommands);
        if ( chosen > 0 ) {
            return chosenCommands.get(chosen - 1);
        } else {
            return "";
        }
    }
}
