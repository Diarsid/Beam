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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class OperationsAnalizer {
    
    private final List<String> executableOperationsByPriority;
    
    OperationsAnalizer() {
        List<String> listOfOperations = new ArrayList<>();
        listOfOperations.add("run");
        listOfOperations.add("start");
        listOfOperations.add("call");
        this.executableOperationsByPriority = 
                Collections.unmodifiableList(listOfOperations);
    }
    
    String tryToChooseByOperationArgumentsComplexity(
            String operation, String command1, String command2) {
        debug("[OPERATIONS ANALIZER] choosing operation arguments by complexity:");
        String args1 = command1.replace(operation, "").trim();
        String args2 = command2.replace(operation, "").trim();
        debug("[OPERATIONS ANALIZER]   - operation  : " + operation);
        debug("[OPERATIONS ANALIZER]   - arguments 1: " + args1);
        debug("[OPERATIONS ANALIZER]   - arguments 2: " + args2);
        if ( this.bothArgumentsAreComposite(args1, args2) ) { 
            debug("[OPERATIONS ANALIZER] both arguments are composite, no choice.");
            return "";
        } else {
            if ( this.bothArgumentsAreWhole(args1, args2)) {
                int arg1Meaning = this.countMeaningfulUnitsInArg(args1);
                int arg2Meaning = this.countMeaningfulUnitsInArg(args2);
                if ( arg1Meaning == arg2Meaning ) {
                    debug("[OPERATIONS ANALIZER] both arguments are whole " +
                            "and have equal meaning weight, no choice.");
                    return "";
                } else {
                    if ( arg1Meaning > arg2Meaning ) {
                        debug("[OPERATIONS ANALIZER] choice: " + command2);
                        return command2;
                    } else {
                        debug("[OPERATIONS ANALIZER] choice: " + command1);
                        return command1;
                    }
                }
            } else {
                if ( this.hasCompositeArgument(args1) ) {
                    debug("[OPERATIONS ANALIZER] choice: " + command2);
                    return command2;
                } else {
                    debug("[OPERATIONS ANALIZER] choice: " + command1);
                    return command1;
                }
            }            
        }
    }
    
    private int countMeaningfulUnitsInArg(String arg) {
        return arg.replace("-", "_").split("_").length;
    }
    
    private boolean bothArgumentsAreWhole(String variant1, String variant2) {
        return ( 
                ( ! variant1.contains(" in ")) && 
                ( ! variant2.contains(" in ")) 
        );
    }
    
    private boolean bothArgumentsAreComposite(String variant1, String variant2) {
        return ( variant1.contains(" in ") && variant2.contains(" in ") );
    }
    
    private boolean hasCompositeArgument(String variant) {
        return variant.contains(" in ");
    }
    
    String defineOperationOf(String command) {
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
    
    private boolean hasOperation(String possibleOperation) {
        return this.executableOperationsByPriority.contains(possibleOperation);
    }
    
    private int priorityOf(String operation) {
        return this.executableOperationsByPriority.indexOf(operation);
    }  
    
    void removeExcessOperations(Map<String, String> chosenCommands) {
        Map<String, String> operationsByTargets = new HashMap<>();
        Map<String, String> weakCommands = new HashMap<>();
        String testedOperation;
        String testedTarget;
        String prevChosenOperation;
        int testedOperationPriority;
        int prevChosenOperationPriority;
        
        for (Entry<String, String> entry : chosenCommands.entrySet()) {
            testedOperation = entry.getKey();            
            if ( this.hasOperation(testedOperation) ) {
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
    
    boolean operationsArgumentsAreEqual(String variant1, String variant2) {
        debug("[OPERATIONS ANALIZER] check arguments equality for pair: " + variant1 + "|" + variant2);
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
                debug("[OPERATIONS ANALIZER] args("+i+"): " + argFrom1 + ", " + argFrom2);
                if ( ! argFrom1.equals(argFrom2) ) {
                    argFrom1isComposite = argFrom1.contains("-");
                    argFrom2isComposite = argFrom2.contains("-");
                    if ( argFrom1isComposite && argFrom2isComposite ) {
                        debug("[OPERATIONS ANALIZER] both args are composite");
                        List<String> partsOfArg1 = Arrays.asList(argFrom1.split("-"));
                        List<String> partsOfArg2 = Arrays.asList(argFrom2.split("-"));
                        debug("[OPERATIONS ANALIZER] arg 1: " + partsOfArg1);
                        debug("[OPERATIONS ANALIZER] arg 2: " + partsOfArg2);
                        operationsArgsAreEqual = 
                                this.argumentPartsAreEqual(partsOfArg1, partsOfArg2);      
                        debug("[OPERATIONS ANALIZER] composite args equality: " + operationsArgsAreEqual);
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
