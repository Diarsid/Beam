/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static diarsid.beam.core.modules.executor.commandscache.ActionRequest.actionRequestOf;
import static diarsid.beam.core.util.Logs.debug;
import static diarsid.beam.core.util.StringUtils.splitBySpaces;

/**
 *
 * @author Diarsid
 */
class CommandsAnalizer {
        
    private final OperationCandidatesResolver candidatesResolver;
    private final ActionsResolver actionsResolver;
    private final OperationsAnalizer operationsAnalizer;
    
    CommandsAnalizer(
            ActionsResolver actionsResolver, 
            OperationsAnalizer operationsAnalizer, 
            OperationCandidatesResolver candidatesResolver) {        
        this.actionsResolver = actionsResolver;
        this.operationsAnalizer = operationsAnalizer;
        this.candidatesResolver = candidatesResolver;
    }
    
    String analizeAndFindAppropriateCommand(String pattern, Map<String, String> commandsRawCache) {
        Map<String, String> commandsByOperations = this.chooseCommandsByOperation(commandsRawCache);
        debug("[COMMANDS ANALIZER] chosen commands: " + commandsByOperations);
        if ( commandsByOperations.size() > 1 ) {
            this.operationsAnalizer.removeExcessOperations(commandsByOperations);
        }                
        debug("[COMMANDS ANALIZER] chosen commands after weakness check: " + commandsByOperations); 
        return this.resolveCommandAfterCacheRefining(pattern, commandsByOperations, commandsRawCache);
    }
    
    private Map<String, String> chooseCommandsByOperation(
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
        
        commandsCacheProcessing: 
        for (Map.Entry<String, String> entry : commandsCache.entrySet()) {
            
            originalTestedCommand = entry.getKey();
            improvedTestedCommand = entry.getValue();
            // these previous values regarded as previous, more optimal values,
            // will be assigned from commandsCache later if necessary.
            originalPrevChosenCommand = "";
            improvedPrevChosenCommand = "";            
            operation = this.operationsAnalizer.defineOperationOf(originalTestedCommand);   
            
            // do not proceed with current command if this particular operation 
            // has been rejected by user during previous iterations.
            if ( operationsExludedFromFurtherProcessing.contains(operation) ) {
                continue commandsCacheProcessing;
            }
            
            if (chosenCommands.containsKey(operation)) {
                // If chosen commands already have command with the same operation
                // it is necessary to find out which command should be picked and saved.
                // Algorithm should find the shortest or the most relevant command among all  
                // other commands having the same operation.
                originalPrevChosenCommand = chosenCommands.get(operation); 
                // Perform smart analisys of operation arguments.
                String madeChoice = this.operationsAnalizer
                        .tryToChooseByOperationArgumentsComplexity(
                                operation,
                                originalTestedCommand,
                                originalPrevChosenCommand);
                if ( ! madeChoice.isEmpty() ) {
                    chosenCommands.put(operation, madeChoice);
                    continue commandsCacheProcessing;
                }
                // TODO
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
                        String chosenCommand = this.tryToChooseCommandForOperation(
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
    
    private String refineCommandFromUnnecessaryParts(String command) {     
        if ( command.contains("webpanel") 
                || command.contains("bookamrks") ) {
            command = command
                    .substring(0, command.indexOf(" - "));
        }
        
        String[] parts = splitBySpaces(command);
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
    
    private String tryToChooseCommandForOperation(
            String operation, String variant1, String variant2) {        
        if ( this.operationsAnalizer.operationsArgumentsAreEqual(variant1, variant2) ) {
            return variant1;
        } else {
            return this.candidatesResolver.resolve(
                    operation, variant1, variant2);
        }        
    }
}
