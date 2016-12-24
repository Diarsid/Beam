/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.context;

import java.util.ArrayList;
import java.util.List;

import old.diarsid.beam.core.modules.DataModule;
import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DaoExecutorIntelligentChoices;
import diarsid.beam.core.modules.executor.workflow.CurrentCommandState;

import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class SmartAmbiguityResolver {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorIntelligentChoices choiceDao;
            
    private boolean active;
    private boolean shouldRememberAutomatically;
    
    SmartAmbiguityResolver(
            DataModule data, 
            IoInnerModule io) {        
        this.ioEngine = io;
        this.active = true;
        this.shouldRememberAutomatically = true;
        this.choiceDao = data.getIntellChoiceDao();
    }
    
    void remember(CurrentCommandState currentCommand) {
        if ( this.shouldRememberAutomatically ) {
            this.choiceDao.saveChoiceForCommandAndItsPart(currentCommand);
        } else {            
            if ( this.askUserIfRememberHisChoiceForThisCommand() ) {
                this.choiceDao.saveChoiceForCommandAndItsPart(currentCommand);
            }
        }
    }
    
    private boolean askUserIfRememberHisChoiceForThisCommand() {
        List<String> yesOrNo = new ArrayList<>();
        yesOrNo.add("yes");
        yesOrNo.add("no");
        int remember = this.ioEngine.resolveVariants(
                "remember your choice for this command?", yesOrNo);
        return ( remember == 1 );
    }
    
    int resolve(
            String question, 
            String command, 
            int resolvingAttemptNumber,
            String patternToResolve, 
            List<String> variants, 
            ContextChoiceSavingCallback contextCallback) {
        if ( this.active ) {
            return this.tryToChoose(
                    question, 
                    command, 
                    resolvingAttemptNumber, 
                    patternToResolve, 
                    variants,
                    contextCallback);
        } else {
            return this.askUserAboutHisChoice(question, variants);
        }
    }
    
    private int tryToChoose(
            String question, 
            String command, 
            int resolvingAttemptNumber,
            String patternToResolve, 
            List<String> variants,
            ContextChoiceSavingCallback contextCallback) {
        
        debug("[RESOLVER] try to resolve: ");
        debug("[RESOLVER]    pattern  : " + patternToResolve);
        debug("[RESOLVER]    command  : " + command);
        debug("[RESOLVER]    attemptN : " + resolvingAttemptNumber);
        debug("[RESOLVER]    variants : " + variants);
        String debugMessage = "[RESOLVER] ";
        
        String choice = this.tryToGuessChoice(variants);
        
        if ( choice.isEmpty() ) {
            debugMessage += "not guessed, ";
            choice = this.choiceDao.getChoiceForCommandPart(
                    command, resolvingAttemptNumber, patternToResolve);
            if (choice.isEmpty()) {
                debugMessage += "not found in memory, ";
                boolean deleted = this.choiceDao.deleteChoicesForCommand(command);
                debugMessage += deleted ? "command removed from memory, " : "";
                contextCallback.saveThisChoice();
                debugMessage += "must ask user and save. ";
                debug(debugMessage);
                return this.askUserAboutHisChoice(question, variants);
            } else {
                debugMessage += "found in memory, ";
                if ( variants.contains(choice) ) {
                    debugMessage += "found in variants, do not save.";
                    contextCallback.doNotSaveThisChoice();
                    debug(debugMessage);
                    return variants.indexOf(choice)+1;
                } else {
                    debugMessage += "does not found in variants, ";
                    boolean deleted = this.choiceDao.deleteChoicesForCommand(command);
                    debugMessage += deleted ? "command removed from memory, " : "";
                    contextCallback.saveThisChoice();
                    debugMessage += "must ask user and save.";
                    debug(debugMessage);
                    return this.askUserAboutHisChoice(question, variants);
                }
            }
        } else {
            debugMessage += "guessed, ";
            if ( variants.contains(choice) ) {
                debugMessage += "found in variants, do not save.";
                contextCallback.doNotSaveThisChoice();
                debug(debugMessage);
                return variants.indexOf(choice)+1;
            } else {
                debugMessage += "does not found in variants, ";
                boolean deleted = this.choiceDao.deleteChoicesForCommand(command);
                debugMessage += deleted ? "command removed from memory, " : "";
                contextCallback.saveThisChoice();
                debugMessage += "must ask user and save.";
                debug(debugMessage);
                return this.askUserAboutHisChoice(question, variants);
            }
        }
    }
    
    private String tryToGuessChoice(List<String> variants) {
        String min = variants.get(0);
        int maxLength = variants.get(0).length();
        int minLength = variants.get(0).length();
        int medium = 0;
        if ( variants.size() > 2 ) {             
            for (int i = 0; i < variants.size(); i++) {
                if (variants.get(i).length() > maxLength) {
                    if (maxLength < minLength) {
                        minLength = maxLength;
                        min = variants.get(i);
                    } 
                    maxLength = variants.get(i).length();
                } else if (variants.get(i).length() < minLength) {
                    minLength = variants.get(i).length();
                    min = variants.get(i);
                } 
                medium = medium + variants.get(i).length();
            }
            medium = (medium - maxLength - minLength) / (variants.size() - 2);
            if ( (minLength*1.0 / medium ) > 0.6 ) {
                return "";
            } else {
                if (min.length() > 1) {
                    return min;
                } else {
                    return "";
                }                
            }
        } else {
            if (variants.get(0).length() > variants.get(1).length()) {
                maxLength = variants.get(0).length();
                minLength = variants.get(1).length();
                min = variants.get(1);
            } else if (variants.get(0).length() < variants.get(1).length()) {
                maxLength = variants.get(1).length();
                minLength = variants.get(0).length();
                min = variants.get(0);
            } else {
                return "";
            }
            if ((minLength * 1.0 / maxLength) > 0.6) {
                return "";
            } else {
                if (min.length() > 1) {
                    return min;
                } else {
                    return "";
                }
            }
        }
    }
    
    private int askUserAboutHisChoice(
            String question, List<String> variants) {
                     
        return this.ioEngine.resolveVariants(question, variants);
    }
    
    List<String> getChoicesByPattern(String pattern) {
        List<CurrentCommandState> commands = this.choiceDao.getChoicesWhereCommandLike(pattern);
        return this.choiceDao.formatCommandsForOutput(commands);
    }
    
    List<String> getAllChoices() {
        return this.choiceDao
                .formatCommandsForOutput(this.choiceDao.getAllChoices());
    }
    
    void setActive(boolean isActive) {
        this.active = isActive;
        if ( isActive ) {
            this.ioEngine.reportMessage("Intelligent command resolving enabled.");
        } else {
            this.ioEngine.reportMessage("Intelligent command resolving disabled.");
        }
    }
    
    void setRememberChoiceAutomatically(boolean autoRemember) {
        this.shouldRememberAutomatically = autoRemember;
        if ( autoRemember ) {
            this.ioEngine.reportMessage(
                    "I will NOT ask before remembering your choice.");            
        } else {
            this.ioEngine.reportMessage(
                    "I will ask before remembering your choice.");
        }        
    }
    
    boolean discardCommandByPattern(String pattern) {
        return this.choiceDao.discardCommandByPattern(pattern);
    }
    
    boolean discardCommandByInvalidLocation(String invalidLocationName) {
        return this.choiceDao.discardCommandByInvalidLocationInPath(invalidLocationName);
    }
    
    boolean discardCommandByPatternAndOperation(String operation, String pattern) {
        return this.choiceDao.discardCommandByPatternAndOperation(operation, pattern);
    }
    
    boolean discardCommandByInvalidTarget(String target) {
        return this.choiceDao.discardCommandByInvalidTargetInPath(target);
    }
    
    boolean deleteChoicesForCommand(String commandPart) {
        List<CurrentCommandState> commands = 
                this.choiceDao.getChoicesWhereCommandLike(commandPart);
        if ( commands.isEmpty() ) {
            this.ioEngine.reportMessage("...command '" + commandPart + "' not found in choices.");
            return false;
        } else if ( commands.size() > 1 ) {
            return this.askUserWhichCommandToDelete(commands);            
        } else {
            String commandToDelete = commands.get(0).getCommandString();
            if ( this.choiceDao.deleteChoicesForCommand(commandToDelete) ) {
                this.ioEngine.reportMessage(
                        "...command '" + commandToDelete + "' removed from choices.");
                return true;
            } else {
                this.ioEngine.reportMessage("...fails to delete.");
                return false;
            }
        }
    }

    private boolean askUserWhichCommandToDelete(List<CurrentCommandState> commands) {
        List<String> displayedCommands = 
                this.choiceDao.formatCommandsForOutput(commands);
        int indexToDelete = this.ioEngine.resolveVariants(
                "...remove from command choices:", displayedCommands);
        if (indexToDelete > 0) {
            String del = commands.get(indexToDelete-1).getCommandString();
            if ( this.choiceDao.deleteChoicesForCommand(del) ) {
                this.ioEngine.reportMessage("...removed.");
                return true;
            } else {
                this.ioEngine.reportMessage("...fails to delete.");
                return false;
            }
        } else {
            return false;
        }
    }
}
