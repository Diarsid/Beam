/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.commandscache.SmartConsoleCommandsCache;
import diarsid.beam.core.modules.executor.context.ExecutorContext;
import diarsid.beam.core.modules.executor.entities.StoredCommandsBatch;
import diarsid.beam.core.modules.executor.processors.ProcessorCommandsBatches;
import diarsid.beam.core.modules.executor.processors.ProcessorLocations;
import diarsid.beam.core.modules.executor.processors.ProcessorNotes;
import diarsid.beam.core.modules.executor.processors.ProcessorPrograms;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;
import diarsid.beam.core.modules.executor.processors.ProcessorsBuilder;
import diarsid.beam.core.modules.executor.workflow.OperationResult;
import diarsid.beam.core.util.Logs;

import static java.lang.String.join;

import static diarsid.beam.core.util.Logs.debug;

/**
 * Implements ExecutorModule interface.
 * 
 * Manages multiple processors that are responsible each for its particular
 * task.
 * 
 * @author Diarsid
 */
class ExecutorModuleWorker implements ExecutorModule {
    
    private final IoInnerModule ioEngine;
    private final ExecutorContext intelligentContext;
    
    private final ProcessorPrograms programs;
    private final ProcessorNotes notes;
    private final ProcessorWebPages pages;
    private final ProcessorLocations locations;
    private final ProcessorCommandsBatches batches; 
    
    private final SmartConsoleCommandsCache commandsCache;
    private final ThreadLocal<Boolean> isCurrentCommandNew;
    
    ExecutorModuleWorker(
            IoInnerModule io,
            ExecutorContext intelligentContext,
            ProcessorsBuilder builder,
            SmartConsoleCommandsCache commandsCache) {
        
        this.ioEngine = io;
        this.intelligentContext = intelligentContext;
        
        this.commandsCache = commandsCache;
        this.isCurrentCommandNew = ThreadLocal.withInitial(() -> true);
        
        this.pages = builder.buildProcessorWebPages();
        this.batches = builder.buildProcessorBatches();
        this.locations = builder.buildProcessorLocations();
        this.programs = builder.buildProcessorPrograms();
        this.notes = builder.buildProcessorNotes();        
    }
    
    @Override
    public void stopModule() {
        // there are nothing stoppable inside of ExecutorModule.
    }  
    
    private void saveConsoleCommandIfValid() {
        if ( this.intelligentContext.ifCanSaveConsoleCommand() &&
                this.isCurrentCommandNew.get() ) {
            this.commandsCache.addCommand(
                    this.intelligentContext.getCurrentCommandFromContext());
        }
    }
    
    private void saveConsoleCommandIfValid(String command) {
        if ( this.intelligentContext.ifCanSaveConsoleCommand() &&
                this.isCurrentCommandNew.get() ) {
            this.commandsCache.addCommand(command);
        }
    }
    
    @Override
    public void open(List<String> commandParams) {
        OperationResult operation = this.locations.open(commandParams);
        if ( operation.ifSuccess() ) {
            this.saveConsoleCommandIfValid();
        } else {
            if ( operation.ifFailCausedByInvalidArgument() ) {
                this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                        "open", operation.getFailureArgument());               
            } else {
                this.intelligentContext.discardCurrentlyExecutedCommandInPattern(
                        join(" ", commandParams));
            }
        }
    }    
    
    @Override
    public List<String> listLocationContent(String locationName) {
        return this.locations.listLocationContent(locationName);
    }
    
    @Override
    public void run(List<String> commandParams) {
        List<OperationResult> operations = 
                this.programs.runPrograms(commandParams);
        OperationResult currentResult;
        for (int i = 0; i < operations.size(); i++) {
            currentResult = operations.get(i);
            if ( currentResult.ifSuccess() ) {
                this.saveConsoleCommandIfValid("run " + commandParams.get(i + 1));
            } else {
                if ( currentResult.ifFailCausedByInvalidArgument() ) {
                    this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                            "run", currentResult.getFailureArgument());
                } else {
                    this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                            "run", commandParams.get(i + 1));
                }
            }         
        }
    }
    
    @Override
    public void start(List<String> commandParams) {
        // command pattern: start [program]
        OperationResult operation = 
                this.programs.runMarkedProgram("start", commandParams); 
        if ( operation.ifSuccess() ) {
            this.saveConsoleCommandIfValid();
        } else {
            if ( operation.ifFailCausedByInvalidArgument() ) {
                this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                        "start", operation.getFailureArgument());
            } else {
                this.intelligentContext.discardCurrentlyExecutedCommandInPattern(
                        join(" ", commandParams));
            }
        }
    }
    
    @Override
    public void stop(List<String> commandParams) {
        // command pattern: stop [program]
        this.programs.runMarkedProgram("stop", commandParams);        
    }
    
    @Override
    public void call(List<String> commandParams) {
        // command pattern: call [command_1] [command_2]...
        StoredCommandsBatch storedBatch;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < commandParams.size(); i++) {
            sb.append(commandParams.get(0))
                    .append(" ")
                    .append(commandParams.get(i));
            storedBatch = this.batches.getBatch(commandParams.get(i));
            if (storedBatch != null) {
                this.executeCommandsBatch(storedBatch);
                this.saveConsoleCommandIfValid(sb.toString());
            }
            sb.delete(0, sb.length());
        }   
    }
    
    @Override
    public void openWebPage(List<String> commandParams) {
        // command pattern: see [page_1] [page_2]...
        List<OperationResult> operations = this.pages.openWebPage(commandParams);
        OperationResult currentResult;
        for (int i = 0; i < operations.size(); i++) {
            currentResult = operations.get(i);
            if (currentResult.ifSuccess()) {
                // save command as: see [page_i]                
                this.saveConsoleCommandIfValid("see " + commandParams.get(i + 1));
            } else { 
                if ( currentResult.ifFailCausedByInvalidArgument() ) {
                    this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                            "see", currentResult.getFailureArgument());
                } else {
                    this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                            "see", commandParams.get(i + 1));
                }    
            }           
        }
    }
    
    @Override
    public void newBatch(List<String> commands, String commandName) {
        this.batches.newBatch(commands, commandName);
    }    
        
    @Override
    public boolean deleteBatch(String commandName) {
        return this.batches.deleteBatch(commandName);
    }    
    
    @Override
    public List<StoredCommandsBatch> getAllBatches() {
        return this.batches.getAllBatches();
    }    
    
    @Override
    public List<StoredCommandsBatch> getBathesByName(String commandName) {
        return this.batches.getBatches(commandName);
    }    
    
    private void executeCommandsBatch(StoredCommandsBatch command) {
        for (String commandString : command.getCommands()) {
            this.dispatchCommandToAppropriateMethod(
                    this.transformCommandStringToParams(commandString));
        }
    } 

    private List<String> transformCommandStringToParams(String commandString) {
        return Arrays.asList(commandString.split("\\s+"));
    }

    private void dispatchCommandToAppropriateMethod(List<String> commandParams) {
        Logs.debug("[EXECUTOR] inner dispatching: " +commandParams);
        this.intelligentContext.adjustCurrentlyExecutedCommand(
                join(" ", commandParams));
        switch (commandParams.get(0)) {
            case "open" :
            case "op" :
            case "o" : {
                this.open(commandParams);
                break;
            }
            case "r" :
            case "run" : {
                this.run(commandParams);
                break;
            }
            case "call" : {
                this.call(commandParams);
                break;
            }
            case "start" : {
                this.start(commandParams);
                break;
            }
            case "stop" : {
                this.stop(commandParams);
                break;
            }
            case "see" :
            case "www" : {
                this.openWebPage(commandParams);
                break;
            }
            case "pause" : {
                this.pauseCommandExecution(commandParams);
                break;
            }
            default : {
                // do nothing
            }
        }
    }
    
    private void pauseCommandExecution(List<String> commandParams) {
        // default value for pause. It will be used in no other value has been 
        // specified explicitly.
        int pause = 5000;
        try {
            // if after 'pause' there are an argument 
            // that contains numbers 0-9 only
            if ( (commandParams.size() > 1) && (commandParams.get(1).matches("\\d+")) ) {
                pause = Integer.parseInt(commandParams.get(1));
            }
            this.ioEngine.reportMessage("pause " + (pause/1000) + " sec...");
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            // nothing to do with it.
        }
    }
    
    @Override
    public void setIntelligentActive(boolean isActive) {
        this.intelligentContext.setContextActive(isActive);
    }  
    
    @Override
    public boolean deleteFromExecutorMemory(String command) {
        if ( command.replace("-", "").length() < 2 ) {
            this.ioEngine.reportMessage("...to short. ");
            return false;
        }
        boolean deletedConsoleMem = this.commandsCache.deleteCached(command);
        boolean deletedChoiceMem = this.intelligentContext.deleteChoicesForCommand(command);
        return ( deletedChoiceMem || deletedConsoleMem );
    }     
    
    @Override
    public void rememberChoiceAutomatically(boolean autoRemember) {
        this.intelligentContext.setRememberChoiceAutomatically(autoRemember);
    }  
    
    @Override
    public Map<String, List<String>> getFromExecutorMemory(String memPattern) {
        Map<String, List<String>> result = new HashMap<>();
        List<String> commandChoices = this.intelligentContext.getChoicesByPattern(memPattern);
        if ( ! commandChoices.isEmpty() ) {
            result.put("commands choices:", commandChoices);
        }
        List<String> consoleCommands = this.commandsCache.getConsoleCommandsOfPattern(memPattern);
        if ( ! consoleCommands.isEmpty() ) {
            result.put("cached console commands:", consoleCommands);
        }
        return result;
    }
    
    /*
    @Override
    public void newNote(List<String> commandParams) {
        if (commandParams.size() > 2) {
            String name = String.join(" ", commandParams.subList(2, commandParams.size()));
            this.system.createAndOpenTxtFileIn(name, this.notes);
        } else {
            this.system.createAndOpenTxtFileIn("", this.notes);
        }
    }
    */
    
    @Override
    public void openNotes() {
        this.notes.openNotes();
    }
    
    @Override
    public void openNote(List<String> commandParams) {
        this.notes.openNote(commandParams);
    }    
    
    @Override
    public void executeIfExists(List<String> commandParams) {
        if ( this.locations.ifCommandLooksLikeLocationAndPath(commandParams) ) {
            this.tryToExecuteAsOpenCommand(commandParams);
        } else {
            this.tryToGetCommandFromCacheAndExecute(commandParams);
        }        
    }

    private void tryToGetCommandFromCacheAndExecute(List<String> commandParams) {
        String cachedCommand = this.commandsCache.getPatternCommandForExecution(
                join(" ", commandParams));
        if ( ! cachedCommand.isEmpty() ) {
            this.executeFoundCachedCommand(cachedCommand);
        } 
    }

    private void tryToExecuteAsOpenCommand(List<String> commandParams) {
        debug("[EXECUTOR] " + commandParams + " will be processed as path");
        List<String> newCommand = this.transformInOpenCommand(commandParams);
        debug("[EXECUTOR] transform to open command : " + newCommand);
        this.intelligentContext.adjustCurrentlyExecutedCommand(join(" ", newCommand));
        this.open(newCommand);
    }
        
    private List<String> transformInOpenCommand(List<String> commandParams) {
        if ( commandParams.size() == 1 ) {
            return Arrays.asList(new String[] {"open", commandParams.get(0)});
        } else {
            return new ArrayList<>();
        }
    }

    private void executeFoundCachedCommand(String cachedCommand) {
        if ( this.ifCachedCommandIsBatch(cachedCommand) ) {
            this.executeStoredBatchIfExists(cachedCommand);
        } else {
            this.executeOldCommandInternally(cachedCommand);
        }
    }
    
    private boolean ifCachedCommandIsBatch(String cachedCommand) {
        return cachedCommand.startsWith("exe ") ||
                cachedCommand.startsWith("call ");
    }

    private void executeStoredBatchIfExists(String cachedCommand) {
        String possibleBatchName = cachedCommand
                .substring(cachedCommand.lastIndexOf(" ")+1);
        StoredCommandsBatch storedBatch = this.batches
                .getBatch(possibleBatchName);
        if ( storedBatch != null ) {
            for (String commandFromBatch : storedBatch.getCommands()) {
                this.executeOldCommandInternally(commandFromBatch);
            }
        } 
    }

    private void executeOldCommandInternally(String obtainedCommand) {
        if ( ! obtainedCommand.isEmpty() ) {
            this.isCurrentCommandNew.set(false);            
            this.dispatchCommandToAppropriateMethod(
                    this.transformCommandStringToParams(obtainedCommand));
            this.isCurrentCommandNew.set(true);
        }        
    }
}