/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    boolean removeFromCacheByCommandName(String command) {
        return this.dao.remove(command);
    }
    
    String getPatternCommandForExecution(String command) {
        return this.intelligentSearchInCache(command);
    }    
    
    private String intelligentSearchInCache(String pattern) {        
        
        if (pattern.length() < 2) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        }
        Map<String, String> commandsCache = 
                this.dao.getImprovedCommandsForPattern(pattern);
        if ( commandsCache.isEmpty() ) {
            return "";
        }
        System.out.println("[COMM CACHE DEBUG] raw commands: " + commandsCache);
        Map<String, String> chosenCommands = new HashMap<>();
        String operationToken;
        for (Map.Entry<String, String> entry : commandsCache.entrySet()) {
            String originalTestedCommand = entry.getKey();
            
            operationToken = this.defineOperationByToken(originalTestedCommand);
            if (chosenCommands.containsKey(operationToken)) {
                if (originalTestedCommand.length() < 
                        chosenCommands.get(operationToken).length()) {
                    chosenCommands.put(operationToken, originalTestedCommand);
                } else if (originalTestedCommand.length() == 
                        chosenCommands.get(operationToken).length()) {
                    if (commandsCache.get(originalTestedCommand).length() < 
                            commandsCache.get(chosenCommands.get(operationToken)).length()) {
                        chosenCommands.put(operationToken, originalTestedCommand);
                    } else if (commandsCache.get(originalTestedCommand).length() == 
                            commandsCache.get(chosenCommands.get(operationToken)).length()) {
                        chosenCommands.put(
                                operationToken, 
                                this.askUserWhichActionToPerform(
                                        commandsCache.get(originalTestedCommand),
                                        commandsCache.get(chosenCommands.get(operationToken))));
                    }
                }
            } else {
                chosenCommands.put(operationToken, originalTestedCommand);
            }
        }
        
        System.out.println("[COMM CACHE DEBUG] chosen commands: " + chosenCommands);
        
        if ( chosenCommands.size() == 1 ) {
            String improvedCommand = commandsCache.get(
                    chosenCommands.entrySet().iterator().next().getValue());
            return this.refineCommandFromUnnecessaryParts(improvedCommand);
        } else {
            String chosenOriginalCommand = this.askUserWhichActionToPerform(
                            new ArrayList<>(chosenCommands.values()));
            String improvedCommand = commandsCache.get(chosenOriginalCommand);
            return this.refineCommandFromUnnecessaryParts(improvedCommand);
        }
    }
    
    private String defineOperationByToken(String command) {
        if (command.startsWith("see ") || 
                command.startsWith("www ") || 
                command.startsWith("web ")) {
            return "see";
        }
        if (command.startsWith("exe ") || 
                command.startsWith("call ")) {
            return "exe";
        }
        if (command.startsWith("o ") || 
                command.startsWith("op ") || 
                command.startsWith("open ")) {
            return "open";
        }
        if (command.startsWith("r ") || 
                command.startsWith("run ")) {
            return "run";
        }
        return command.substring(0, command.indexOf(" "));
    }

    private String refineCommandFromUnnecessaryParts(String command) {     
        if ( command.contains("webpanel") 
                || command.contains("bookamrks") ) {
            command = command
                    .substring(0, command.indexOf(" - "));
        }
        
        String[] parts = command.split("\\s+");
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
    
    private String askUserWhichActionToPerform(String variant1, String variant2) {
        return this.askUserWhichActionToPerform(
                Arrays.asList(new String[]{variant1, variant2}));
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
