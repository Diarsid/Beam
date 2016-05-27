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
        implements IntelligentExecutorCommandContext, 
                   CurrentlyExecutedCommandContext{
    
    private final ThreadLocal<CurrentCommandState> currentCommand;
    private final ThreadLocal<Boolean> needSaveChoices;
    private final ThreadLocal<Boolean> currentCommandDiscarded;
    private final ThreadLocal<Integer> resolvingAttemptNubmer;
    private final IntelligentExecutorResolver resolver;
    private final CurrentlyExecutedCommandContextCallback contextCallback;
    
    CurrentlyExecutedCommandIntelligentContext(IntelligentExecutorResolver resolver) {
        this.currentCommand = new ThreadLocal<>();
        this.needSaveChoices = new ThreadLocal<>();
        this.currentCommandDiscarded = new ThreadLocal<>();
        this.resolvingAttemptNubmer = new ThreadLocal<>();
        this.resolver = resolver;
        this.contextCallback = new CurrentlyExecutedCommandContextCallback() {
            @Override
            public void choiceWasAlreadySaved() {
                needSaveChoices.set(false);
            }
        };
    }
    
    @Override
    public void beginCurrentCommandState(List<String> commandParams) {
        System.out.println("[EXECUTOR PROXY] command intercepted!");
        System.out.println("[EXECUTOR PROXY] " + commandParams.toString());
        this.currentCommand.set(new CurrentCommandState(commandParams));
        this.currentCommandDiscarded.set(false);
        this.needSaveChoices.set(true);
        this.resolvingAttemptNubmer.set(0);
    }
    
    @Override
    public void destroyCurrentCommandState() {
        System.out.println("[EXECUTOR PROXY] end of interception...");
        System.out.println("[EXECUTOR PROXY] " + currentCommand.get().getCommandString());
        System.out.println();
        this.rememberChoicesForCurrentCommandIfNecessary();
        this.currentCommand.remove();
    }
    
    private void rememberChoicesForCurrentCommandIfNecessary() {
        System.out.println("[EXECUTOR PROXY] remeber?");
        if ( this.ifMustSaveChoices() ) {
            this.resolver.remember(this.currentCommand.get());
        }
    }
    
    private boolean ifMustSaveChoices() {
        System.out.println("[EXECUTOR PROXY] hasChoices="+this.currentCommand.get().hasChoices()
                + ", needSaveChoices="+this.needSaveChoices.get()+
                ", commandValid="+!this.currentCommandDiscarded.get());
        return 
                this.currentCommand.get().hasChoices() && 
                this.needSaveChoices.get() &&
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
        
        if ( this.needSaveChoices.get() ) {
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
        this.currentCommand.set(
                new CurrentCommandState(Arrays.asList(newCommand)));
    }
    
    @Override
    public void adjustCurrentlyExecutedCommand(String newCommand) {
        this.currentCommand.set(new CurrentCommandState(newCommand));
    }
    
    @Override
    public void discardCurrentlyExecutedCommandInPattern(String pattern) {
        System.out.println("[EXECUTOR DEBUG] discard pattern: " + pattern);
        boolean deleted = this.resolver.discardCommandByPattern(pattern);
        System.out.println("[EXECUTOR DEBUG] discarded? " + deleted);
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
