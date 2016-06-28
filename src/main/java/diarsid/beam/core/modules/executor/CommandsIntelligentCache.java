/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import diarsid.beam.core.Logs;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;

/**
 *
 * @author Diarsid
 */
class CommandsIntelligentCache {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorConsoleCommands dao;
    private final List<String> executableOperationsByPriority;
        
    CommandsIntelligentCache(
            IoInnerModule ioEngine,
            DaoExecutorConsoleCommands consoleCommandsDao) {  
        
        this.ioEngine = ioEngine;
        this.dao = consoleCommandsDao;
        List<String> listOfOperations = new ArrayList<>();
        listOfOperations.add("run");
        listOfOperations.add("start");
        listOfOperations.add("call");
        this.executableOperationsByPriority = 
                Collections.unmodifiableList(listOfOperations);
    }    
    
    void addCommand(List<String> commandParams) {
        this.dao.saveNewConsoleCommand(String.join(" ", commandParams));
    }
    
    void addCommand(String command) {
        this.dao.saveNewConsoleCommand(command);
    }
    
    String getPatternCommandForExecution(String command) {
        if ( command.length() < 2 ) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        } else {
            return this.intelligentSearchInCache(command);
        }        
    }    
    
    private String intelligentSearchInCache(String pattern) {    
        Map<String, String> commandsRawCache = this.dao.getImprovedCommandsForPattern(pattern);
        if ( commandsRawCache.isEmpty() ) {
            return "";
        }
        Logs.debug("[COMMANDS CACHE] raw commands: " + commandsRawCache);
        Map<String, String> chosenCommands = 
                this.chooseCommandsByOperationFromRawCache(commandsRawCache);
        Logs.debug("[COMMANDS CACHE] chosen commands: " + chosenCommands);
        if ( chosenCommands.size() > 1 ) {
            this.testCommandsForWeakCombinations2(chosenCommands);
        }                
        Logs.debug("[COMMANDS CACHE] chosen commands after weakness check: " + chosenCommands); 
        return this.resolveCommandAfterCacheRefining(chosenCommands, commandsRawCache);
    }

    private Map<String, String> chooseCommandsByOperationFromRawCache(
            Map<String, String> commandsCache) {
        
        // commandsCache contains Map of commands where key is a 
        // cached command itself as it was once printed by user. 
        // This command could be made of abbreviations and thus
        // be quite short.
        // Value is an 'improved' version of cached command placed in key,
        //  where all abbreviations have been replaced with full names
        // and arguments
        
        Map<String, String> chosenCommands = new HashMap<>();
        String operation;
        String originalTestedCommand;
        String improvedTestedCommand;
        String originalPrevChosenCommand;
        String improvedPrevChosenCommand;
        
        for (Map.Entry<String, String> entry : commandsCache.entrySet()) {
            
            originalTestedCommand = entry.getKey();
            improvedTestedCommand = entry.getValue();
            originalPrevChosenCommand = "";
            improvedPrevChosenCommand = "";            
            operation = this.defineOperationOf(originalTestedCommand);   
            
            if (chosenCommands.containsKey(operation)) {
                // If chosen commands already have command with the same operation
                // it is necessary to find out which command should be picked and saved.
                // Algorithm should find the shortest command among all candidate 
                // commands having the same operation.
                originalPrevChosenCommand = chosenCommands.get(operation);                
                if (originalTestedCommand.length() < originalPrevChosenCommand.length()) {
                    // replace command because new tested candidate is shorter
                    // than previous one
                    chosenCommands.put(operation, originalTestedCommand);
                } else if (originalTestedCommand.length() == originalPrevChosenCommand.length()) {
                    // if both tested and previously chosen command have equal length
                    // let's see on the length difference between their 'improved'
                    // versions as those commands normally have greater length than 
                    // 'original' versions
                    improvedPrevChosenCommand = commandsCache.get(originalPrevChosenCommand);
                    if (improvedTestedCommand.length() < improvedPrevChosenCommand.length()) {
                        // replace prveiously chosen command because 'improved' version
                        // of currently tested command is shorter than 'improved' version
                        // of previosly chosen one
                        chosenCommands.put(operation, originalTestedCommand);
                    } else if (improvedTestedCommand.length() == improvedPrevChosenCommand.length()) {
                        // if even 'improved' versions of both tested and previously chosen
                        // commands have equal length then ask user about his choice
                        // among these two commands
                        String chosenCommand = this.askUserWhichActionToPerform(
                                "chose candidate for '" + operation + "' command:",
                                improvedTestedCommand,
                                improvedPrevChosenCommand);
                        if ( chosenCommand.isEmpty() ) {
                            // if user has not made any choice (it means he has rejected
                            // both variants!) then remove this hateful command at all.
                            chosenCommands.remove(operation);
                        } else {
                            // save user's choice to proceed
                            if ( chosenCommand.equals(improvedTestedCommand) ) {
                                chosenCommands.put(operation, originalTestedCommand);
                            } else {
                                chosenCommands.put(operation, originalPrevChosenCommand);
                            }
                        }                        
                    }
                } else {
                    // do nothing.
                }
            } else {
                chosenCommands.put(operation, originalTestedCommand);
            }
        }
        return chosenCommands;
    }
    
    private void testCommandsForWeakCombinations2(Map<String, String> chosenCommands) {
        Map<String, String> operationsByTargets = new HashMap<>();
        Map<String, String> weakCommands = new HashMap<>();
        String testedOperation;
        String testedTarget;
        String prevChosenOperation;
        int testedOperationPriority;
        int prevChosenOperationPriority;
        
        for (Entry<String, String> entry : chosenCommands.entrySet()) {
            testedOperation = entry.getKey();            
            if ( this.executableOperationsByPriority.contains(testedOperation) ) {
                testedTarget = ( entry.getValue() ).substring(entry.getValue().indexOf(" "));
                if ( operationsByTargets.containsKey(testedTarget) ) {
                    prevChosenOperation = operationsByTargets.get(testedTarget);
                    testedOperationPriority = this.priorityOf(testedOperation);
                    prevChosenOperationPriority = this.priorityOf(prevChosenOperation);
                    if ( testedOperationPriority > prevChosenOperationPriority ) {
                        weakCommands.put(
                                prevChosenOperation, chosenCommands.get(prevChosenOperation));
                        operationsByTargets.replace(testedTarget, testedOperation);
                    } else {
                        weakCommands.put(
                                testedOperation, chosenCommands.get(testedOperation));
                    }                  
                } else {
                    operationsByTargets.put(testedTarget, testedOperation);
                }
            }            
        }
        
        for (Entry<String, String> entry : weakCommands.entrySet()) {
            chosenCommands.remove(entry.getKey(), entry.getValue());
        }
    }

    private int priorityOf(String operation) {
        return this.executableOperationsByPriority.indexOf(operation);
    }  

    private String resolveCommandAfterCacheRefining(
            Map<String, String> chosenCommands, Map<String, String> commandsCache) {
        if ( chosenCommands.size() == 1 ) {
            String chosenCommand = chosenCommands.entrySet().iterator().next().getValue();
            if ( chosenCommand.isEmpty() ) {
                return "";
            } else {
                String improvedCommand = commandsCache.get(chosenCommand);
                return this.refineCommandFromUnnecessaryParts(improvedCommand);
            }
        } else if ( chosenCommands.size() > 1 ) {
            String chosenOriginalCommand = this.askUserWhichActionToPerform(
                    "action?",
                    new ArrayList<>(chosenCommands.values()));
            if ( chosenOriginalCommand.isEmpty() ) {
                return "";
            } else {
                String improvedCommand = commandsCache.get(chosenOriginalCommand);
                return this.refineCommandFromUnnecessaryParts(improvedCommand);
            }            
        } else {
            return "";
        }
    }
    
    private String defineOperationOf(String command) {
        if (command.startsWith("see ") || 
                command.startsWith("www ") || 
                command.startsWith("web ")) {
            return "see";
        }
        if (command.startsWith("exe ") || 
                command.startsWith("call ")) {
            return "call";
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
    
    private String askUserWhichActionToPerform(String question, String variant1, String variant2) {
        return this.askUserWhichActionToPerform(
                question, 
                Arrays.asList(new String[]{variant1, variant2}));
    }

    private String askUserWhichActionToPerform(String question, List<String> chosenCommands) {
        int chosen = this.ioEngine.resolveVariantsWithExternalIO(
                question, chosenCommands);
        if ( chosen > 0 ) {
            return chosenCommands.get(chosen - 1);
        } else {
            return "";
        }
    }
}
