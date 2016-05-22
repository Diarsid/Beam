/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.IoInnerModule;

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
    private final ProcessorCommands commands;    
    
    ExecutorModuleWorker(
            IoInnerModule io,
            IntelligentExecutorCommandContext intell,
            ProcessorsBuilder builder) {
        
        this.ioEngine = io;
        this.intelligentContext = intell;
        this.pages = builder.buildProcessorWebPages();
        this.commands = builder.buildProcessorCommands();
        this.locations = builder.buildProcessorLocations();
        this.programs = builder.buildProcessorPrograms();
        this.notes = builder.buildProcessorNotes();
    }
    
    @Override
    public void stopModule() {
        
    }
    
    @Override
    public void open(List<String> commandParams) {
        this.locations.open(commandParams);
    }    
    
    @Override
    public List<String> listLocationContent(String locationName) {
        return this.locations.listLocationContent(locationName);
    }
    
    @Override
    public void run(List<String> commandParams) {
        this.programs.runProgram(commandParams);
    }
    
    @Override
    public void start(List<String> commandParams) {
        // command pattern: start [program]
        this.programs.runMarkedProgram("start", commandParams);       
    }
    
    @Override
    public void stop(List<String> commandParams) {
        // command pattern: stop [program]
        this.programs.runMarkedProgram("stop", commandParams);        
    }
    
    @Override
    public void call(List<String> commandParams) {
        // command pattern: call [command_1] [command_2]...
        StoredExecutorCommand storedCommand;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < commandParams.size(); i++) {
            sb.append(commandParams.get(0))
                    .append(" ")
                    .append(commandParams.get(i));
            storedCommand = this.commands.getCommand(commandParams.get(i));
            if (storedCommand != null) {
                this.executeCommand(storedCommand);
            }
        }   
    }
    
    @Override
    public void openWebPage(List<String> commandParams) {
        this.pages.openWebPage(commandParams);
    }
    
    @Override
    public void newCommand(List<String> commands, String commandName) {
        this.commands.newCommand(commands, commandName);
    }    
        
    @Override
    public boolean deleteCommand(String commandName) {
        return this.commands.deleteCommand(commandName);
    }    
    
    @Override
    public List<StoredExecutorCommand> getAllCommands() {
        return this.commands.getAllCommands();
    }    
    
    @Override
    public List<StoredExecutorCommand> getCommands(String commandName) {
        return this.commands.getCommands(commandName);
    }    
    
    private void executeCommand(StoredExecutorCommand command) {
        List<String> commandParams;
        for(String commandString : command.getCommands()) {
            commandParams = Arrays.asList(commandString.split("\\s+"));
            switch(commandParams.get(0)) {
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
                    this.ioEngine.reportError("Unrecognizible command.");
                }
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
        this.intelligentContext.setActive(isActive);
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
}