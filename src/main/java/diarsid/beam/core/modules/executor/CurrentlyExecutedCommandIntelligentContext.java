/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.Logs;
import diarsid.beam.core.modules.executor.workflow.CurrentCommandState;

/**
 *
 * @author Diarsid
 */
class CurrentlyExecutedCommandIntelligentContext 
        implements IntelligentExecutorCommandContext, 
                   CurrentlyExecutedCommandContext{
    
    private final ThreadLocal<CurrentCommandState> currentCommand;
    private final ThreadLocal<Boolean> needSaveCurrentChoice;
    private final ThreadLocal<Boolean> currentCommandDiscarded;
    private final ThreadLocal<Integer> resolvingAttemptNubmer;
    private final IntelligentExecutorResolver resolver;
    private final CurrentlyExecutedCommandContextCallback contextCallback;
    
    CurrentlyExecutedCommandIntelligentContext(IntelligentExecutorResolver resolver) {
        this.currentCommand = new ThreadLocal<>();
        this.needSaveCurrentChoice = new ThreadLocal<>();
        this.currentCommandDiscarded = new ThreadLocal<>();
        this.resolvingAttemptNubmer = new ThreadLocal<>();
        this.resolver = resolver;
        this.contextCallback = new CurrentlyExecutedCommandContextCallback() {
            @Override
            public void doNotSaveThisChoice() {
                needSaveCurrentChoice.set(false);
            }
            
            @Override
            public void saveThisChoice() {
                needSaveCurrentChoice.set(true);
            }
        };
    }
    
    @Override
    public void beginCurrentCommandState(List<String> commandParams) {
        Logs.debug("[EXECUTOR CONTEXT] command intercepted : "  + commandParams.toString());
        this.setContextAfresh(commandParams);
    }

    private void setContextAfresh(List<String> commandParams) {
        Logs.debug("[EXECUTOR CONTEXT] context refreshed: " + commandParams);
        this.currentCommand.set(new CurrentCommandState(commandParams));
        this.currentCommandDiscarded.set(false);
        this.needSaveCurrentChoice.set(true);
        this.resolvingAttemptNubmer.set(0);
    }
    
    private void setContextAfresh(String command) {
        Logs.debug("[EXECUTOR CONTEXT] context refreshed: " + command);
        this.currentCommand.set(new CurrentCommandState(command));
        this.currentCommandDiscarded.set(false);
        this.needSaveCurrentChoice.set(true);
        this.resolvingAttemptNubmer.set(0);
    }
    
    @Override
    public void destroyCurrentCommandState() {
        String comm = currentCommand.get().getCommandString();        
        this.rememberChoicesForCurrentCommandIfNecessary();
        this.clearContext();
        Logs.debug("[EXECUTOR CONTEXT] end of interception...");
    }

    private void clearContext() {
        this.currentCommand.remove();
        this.currentCommandDiscarded.set(false);
        this.needSaveCurrentChoice.set(true);
        this.resolvingAttemptNubmer.set(0);
    }
    
    private void rememberChoicesForCurrentCommandIfNecessary() {
        Logs.debug("[EXECUTOR CONTEXT] remeber '" + this.currentCommand.get().getCommandString() + "' ?" );
        if ( this.ifMustSaveChoices() ) {
            this.resolver.remember(this.currentCommand.get());
        }
    }
    
    private boolean ifMustSaveChoices() {
        Logs.debug("[EXECUTOR CONTEXT] hasChoices="+this.currentCommand.get().hasChoices()+
                ", commandValid="+!this.currentCommandDiscarded.get());
        return 
                this.currentCommand.get().hasChoices() &&
                ! this.currentCommandDiscarded.get();
    }
    
    @Override
    public int resolve(
            String question, String patternToResolve, List<String> variants) {        
        
        int chosenVariant = this.resolver.resolve(
                question, 
                this.getCommandStringFromContext(), 
                this.getResolvingAttemptNumberDuringContextSession(),
                patternToResolve, 
                variants,
                this.contextCallback);    
        this.incrementResolvingAttemptNumber();
        this.saveChoiceInCurrentContextIfMade(patternToResolve, chosenVariant, variants);        
        return chosenVariant;
    }

    private void incrementResolvingAttemptNumber() {
        this.resolvingAttemptNubmer.set(this.resolvingAttemptNubmer.get() + 1);
    }
    
    private String getCommandStringFromContext() {
        return this.currentCommand.get().getCommandString();
    }
    
    private int getResolvingAttemptNumberDuringContextSession() {
        return this.resolvingAttemptNubmer.get();
    }
    
    private void saveChoiceInCurrentContextIfMade(
            String patternToResolve, int chosenVariant, List<String> variants) {
        
        if ( this.needSaveCurrentChoice.get() ) {
            if ( chosenVariant > 0 ) {
                this.currentCommand.get().addChoice(
                        patternToResolve, variants.get(chosenVariant-1));
            }
        } 
    }
    
    @Override
    public void setContextActive(boolean active) {
        this.resolver.setActive(active);
    }
        
    @Override
    public void adjustCurrentlyExecutedCommand(String... newCommand) {
        this.rememberChoicesForCurrentCommandIfNecessary();
        this.setContextAfresh(Arrays.asList(newCommand));
    }
    
    @Override
    public void adjustCurrentlyExecutedCommand(String newCommand) {
        this.rememberChoicesForCurrentCommandIfNecessary();
        this.setContextAfresh(newCommand);
    }
    
    @Override
    public void discardCurrentlyExecutedCommandInPattern(String pattern) {
        Logs.debug("[EXECUTOR CONTEXT] discard pattern: " + pattern);
        boolean deleted = this.resolver.discardCommandByPattern(pattern);
        Logs.debug("[EXECUTOR CONTEXT] discarded? " + deleted);
        this.currentCommandDiscarded.set(true);
    }
    
    @Override
    public void discardCurrentlyExecutedCommandInPatternAndOperation(
            String operation, String pattern) {
        Logs.debug("[EXECUTOR CONTEXT] discard operation + pattern: " + operation + " + " + pattern);
        boolean deleted = this.resolver.discardCommandByPatternAndOperation(operation, pattern);
        Logs.debug("[EXECUTOR CONTEXT] discarded? " + deleted);
        this.currentCommandDiscarded.set(true);
    }
    
    @Override
    public boolean ifCanSaveConsoleCommand() {
        return ! this.currentCommandDiscarded.get();
    }
    
    @Override
    public boolean deleteChoicesForCommand(String command) {
        return this.resolver.deleteChoicesForCommand(command);
    }
    
    @Override
    public List<String> getAllChoices() {
        return this.resolver.getAllChoices();
    }
    
    @Override
    public void setRememberChoiceAutomatically(boolean auto) {
        this.resolver.setRememberChoiceAutomatically(auto);
    }
}
