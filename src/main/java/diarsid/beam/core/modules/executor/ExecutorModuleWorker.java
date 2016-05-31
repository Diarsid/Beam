/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.entities.StoredCommandsBatch;
import diarsid.beam.core.modules.executor.processors.ProcessorCommandsBatches;
import diarsid.beam.core.modules.executor.processors.ProcessorLocations;
import diarsid.beam.core.modules.executor.processors.ProcessorNotes;
import diarsid.beam.core.modules.executor.processors.ProcessorPrograms;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;
import diarsid.beam.core.modules.executor.processors.ProcessorsBuilder;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

import static java.lang.String.join;

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
    private final IntelligentExecutorCommandContext intelligentContext;
    
    private final ProcessorPrograms programs;
    private final ProcessorNotes notes;
    private final ProcessorWebPages pages;
    private final ProcessorLocations locations;
    private final ProcessorCommandsBatches commands; 
    
    private final CommandsIntelligentCache commandsCache;
    private final ThreadLocal<Boolean> isCurrentCommandNew;
    
    ExecutorModuleWorker(
            IoInnerModule io,
            IntelligentExecutorCommandContext intelligentContext,
            ProcessorsBuilder builder,
            CommandsIntelligentCache commandsCache) {
        
        this.ioEngine = io;
        this.intelligentContext = intelligentContext;
        
        this.commandsCache = commandsCache;
        this.isCurrentCommandNew = ThreadLocal.withInitial(
                new Supplier<Boolean> () {
            @Override
            public Boolean get() {
                return true;
            }
        });
        
        this.pages = builder.buildProcessorWebPages();
        this.commands = builder.buildProcessorBatches();
        this.locations = builder.buildProcessorLocations();
        this.programs = builder.buildProcessorPrograms();
        this.notes = builder.buildProcessorNotes();        
    }
    
    @Override
    public void stopModule() {
        // there are nothing stoppable inside of ExecutorModule.
    }  
    
    private void saveConsoleCommandIfValid(List<String> commandParams) {
        if ( this.intelligentContext.ifCanSaveConsoleCommand() &&
                this.isCurrentCommandNew.get() ) {
            this.commandsCache.addCommand(commandParams);
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
        if (operation.ifSuccess()) {
            this.saveConsoleCommandIfValid(commandParams);
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
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i).ifSuccess()) {
                this.saveConsoleCommandIfValid("run " + commandParams.get(i+1));
            }            
        }
    }
    
    @Override
    public void start(List<String> commandParams) {
        // command pattern: start [program]
        OperationResult operation = 
                this.programs.runMarkedProgram("start", commandParams); 
        if (operation.ifSuccess()) {
            this.saveConsoleCommandIfValid(commandParams);
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
        StoredCommandsBatch storedCommand;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < commandParams.size(); i++) {
            sb.append(commandParams.get(0))
                    .append(" ")
                    .append(commandParams.get(i));
            storedCommand = this.commands.getBatch(commandParams.get(i));
            if (storedCommand != null) {
                this.executeCommand(storedCommand);
                this.saveConsoleCommandIfValid(sb.toString());
            }
            sb.delete(0, sb.length());
        }   
    }
    
    @Override
    public void openWebPage(List<String> commandParams) {
        // command pattern: see [page_1] [page_2]...
        List<OperationResult> operations = this.pages.openWebPage(commandParams);
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i).ifSuccess()) {
                // save command as: see [page_i]
                this.saveConsoleCommandIfValid("see " + commandParams.get(i+1));
            }            
        }
    }
    
    @Override
    public void newCommand(List<String> commands, String commandName) {
        this.commands.newBatch(commands, commandName);
    }    
        
    @Override
    public boolean deleteCommand(String commandName) {
        return this.commands.deleteBatch(commandName);
    }    
    
    @Override
    public List<StoredCommandsBatch> getAllCommands() {
        return this.commands.getAllBatches();
    }    
    
    @Override
    public List<StoredCommandsBatch> getCommands(String commandName) {
        return this.commands.getBatches(commandName);
    }    
    
    private void executeCommand(StoredCommandsBatch command) {
        for (String commandString : command.getCommands()) {
            this.dispatchCommandToAppropriateMethod(
                    this.transformCommandStringToParams(commandString));
        }
    } 

    private List<String> transformCommandStringToParams(String commandString) {
        return Arrays.asList(commandString.split("\\s+"));
    }

    private void dispatchCommandToAppropriateMethod(List<String> commandParams) {
        System.out.println("[EXECUTOR DEBUG] dispatch: " +commandParams);
        this.intelligentContext
                    .adjustCurrentlyExecutedCommand(aggregate(commandParams));
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
    public boolean deleteMem(String command) {
        return this.intelligentContext.deleteChoicesForCommand(command);
    }     
    
    @Override
    public void rememberChoiceAutomatically(boolean autoRemember) {
        this.intelligentContext.setRememberChoiceAutomatically(autoRemember);
    }  
    
    @Override
    public List<String> getAllChoices() {
        return this.intelligentContext.getAllChoices();
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
        String cachedCommand = this.commandsCache
                .getPatternCommandForExecution(this.aggregate(commandParams));
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
        StoredCommandsBatch storedBatch = this.commands
                .getBatch(possibleBatchName);
        if ( storedBatch != null ) {
            for (String commandFromBatch : storedBatch.getCommands()) {
                this.executeOldCommandInternally(commandFromBatch);
            }
        } else {
            this.commandsCache.removeFromCacheByCommandName(possibleBatchName);
        }
    }

    private String aggregate(List<String> commandParams) {
        return join(" ", commandParams);
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