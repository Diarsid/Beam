/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;
import diarsid.beam.core.util.Logs;

import static diarsid.beam.core.modules.executor.commandscache.ActionRequest.actionRequestOf;
import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class SmartConsoleCommandsCacheWorker implements SmartConsoleCommandsCache {
    
    private final DaoExecutorConsoleCommands dao;
    private final ActionsResolver actionsResolver;
    private final List<String> executableOperationsByPriority;
        
    SmartConsoleCommandsCacheWorker(
            ActionsResolver actionsResolver,
            DaoExecutorConsoleCommands consoleCommandsDao) {  
        
        this.actionsResolver = actionsResolver;
        this.dao = consoleCommandsDao;
        List<String> listOfOperations = new ArrayList<>();
        listOfOperations.add("run");
        listOfOperations.add("start");
        listOfOperations.add("call");
        this.executableOperationsByPriority = 
                Collections.unmodifiableList(listOfOperations);
    }    
    
    @Override
    public void addCommand(List<String> commandParams) {
        this.dao.saveNewConsoleCommand(String.join(" ", commandParams));
    }
    
    @Override
    public void addCommand(String command) {
        this.dao.saveNewConsoleCommand(command);
    }
    
    @Override
    public boolean deleteCommand(String command) {
        return this.dao.remove(command);
    }
    
    @Override
    public String getPatternCommandForExecution(String command) {
        if ( command.length() < 2 ) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        } else {
            return this.intelligentSearchInCache(command);
        }        
    }    
    
    private String intelligentSearchInCache(String pattern) {    
        debug("[COMMANDS CACHE] search for pattern: " + pattern);
        Map<String, String> commandsRawCache = this.dao.getImprovedCommandsForPattern(pattern);
        if ( commandsRawCache.isEmpty() ) {
            return "";
        }
        debug("[COMMANDS CACHE] raw commands: " + commandsRawCache);
        Map<String, String> chosenCommands = 
                this.chooseCommandsByOperationFromRawCache(commandsRawCache);
        debug("[COMMANDS CACHE] chosen commands: " + chosenCommands);
        if ( chosenCommands.size() > 1 ) {
            this.testCommandsForWeakCombinations2(chosenCommands);
        }                
        debug("[COMMANDS CACHE] chosen commands after weakness check: " + chosenCommands); 
        return this.resolveCommandAfterCacheRefining(pattern, chosenCommands, commandsRawCache);
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
        Set<String> operationsExludedFromFurtherProcessing = new HashSet<>();
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
            
            if ( operationsExludedFromFurtherProcessing.contains(operation) ) {
                continue;
            }
            
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
                        String chosenCommand = this.askUserToChooseCandidateActionForOperation(
                                operation, improvedTestedCommand, improvedPrevChosenCommand);
                        if ( chosenCommand.isEmpty() ) {
                            // if user has not made any choice (it means he has rejected
                            // both variants!) then remove this hateful command at all.
                            chosenCommands.remove(operation);
                            // Do not process any futher commands with this operation 
                            // during current cache processing.
                            operationsExludedFromFurtherProcessing.add(operation);
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
            String pattern, Map<String, String> chosenCommands, Map<String, String> commandsCache) {
        if ( chosenCommands.size() == 1 ) {
            String chosenCommand = chosenCommands.entrySet().iterator().next().getValue();
            if ( chosenCommand.isEmpty() ) {
                return "";
            } else {
                String improvedCommand = commandsCache.get(chosenCommand);
                return this.refineCommandFromUnnecessaryParts(improvedCommand);
            }
        } else if ( chosenCommands.size() > 1 ) {
            String chosenOriginalCommand = this.actionsResolver.resolve(actionRequestOf(
                    pattern, chosenCommands.values()));
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
    
    private String askUserToChooseCandidateActionForOperation(
            String operation, String variant1, String variant2) {        
        if ( this.operationsArgumentsAreEqual(variant1, variant2) ) {
            return variant1;
        } else {
            return this.actionsResolver.resolveActionsCandidates(
                    operation, Arrays.asList(new String[] {variant1, variant2}));
        }        
    }

    private boolean operationsArgumentsAreEqual(String variant1, String variant2) {
        Logs.debug("[COMMANDS CACHE] check arguments equality for pair: " + variant1 + "|" + variant2);
        List<String> argsOfCommand1 = new ArrayList(Arrays.asList(variant1.split("\\s+")));
        List<String> argsOfCommand2 = new ArrayList(Arrays.asList(variant2.split("\\s+")));        
        argsOfCommand1.remove(0);
        argsOfCommand2.remove(0);        
        if ( argsOfCommand1.size() == argsOfCommand2.size() ) {
            boolean operationsArgsAreEqual = false;
            String argFrom1;
            String argFrom2;
            boolean argFrom1isComposite;
            boolean argFrom2isComposite;
            for (int i = 0; i < argsOfCommand1.size(); i++) {
                argFrom1 = argsOfCommand1.get(i);
                argFrom2 = argsOfCommand2.get(i);
                Logs.debug("[COMMANDS CACHE] args("+i+"): " + argFrom1 + ", " + argFrom2);
                if ( ! argFrom1.equals(argFrom2) ) {
                    argFrom1isComposite = argFrom1.contains("-");
                    argFrom2isComposite = argFrom2.contains("-");
                    if ( argFrom1isComposite && argFrom2isComposite ) {
                        Logs.debug("[COMMANDS CACHE] both args are composite");
                        List<String> partsOfArg1 = Arrays.asList(argFrom1.split("-"));
                        List<String> partsOfArg2 = Arrays.asList(argFrom2.split("-"));
                        Logs.debug("[COMMANDS CACHE] arg 1: " + partsOfArg1);
                        Logs.debug("[COMMANDS CACHE] arg 2: " + partsOfArg2);
                        operationsArgsAreEqual = 
                                this.argumentPartsAreEqual(partsOfArg1, partsOfArg2);      
                        Logs.debug("[COMMANDS CACHE] composite args equality: " + operationsArgsAreEqual);
                    } else {
                        return false;
                    }
                } 
            }
            return operationsArgsAreEqual;
        } else {
            return false;
        }        
    }

    private boolean argumentPartsAreEqual(
            List<String> partsOfArg1, List<String> partsOfArg2) {
        return (partsOfArg1.containsAll(partsOfArg2) && partsOfArg2.containsAll(partsOfArg1));
    }
}
