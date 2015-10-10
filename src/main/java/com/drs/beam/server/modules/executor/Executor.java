/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.drs.beam.server.entities.command.StoredExecutorCommand;
import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.executor.handlers.CommandsHandler;
import com.drs.beam.server.modules.executor.handlers.CommandsHandlerWorker;
import com.drs.beam.server.modules.executor.handlers.LocationsHandler;
import com.drs.beam.server.modules.executor.handlers.LocationsHandlerWorker;
import com.drs.beam.server.modules.executor.os.OS;
import com.drs.beam.server.modules.io.InnerIOModule;

public class Executor implements ExecutorModule{
    // Fields =============================================================================
    private static Executor executor;
    
    private final InnerIOModule ioEngine;
    private final OS system;
    private final LocationsHandler locationsHandler;
    private final CommandsHandler commandsHandler;
    
    // Constructors =======================================================================
    private Executor(InnerIOModule io, DataManagerModule data) {
        this.ioEngine = io;
        this.locationsHandler = new LocationsHandlerWorker(data.getLocationsDao(), this.ioEngine);
        this.commandsHandler = new CommandsHandlerWorker(data.getCommandsDao(), this.ioEngine);
        this.system = OS.getOS(io);
    }

    // Methods ============================================================================
    
    public static void initAndRegister(InnerIOModule innerIo, DataManagerModule data){
        if (executor == null){
            executor = new Executor(innerIo, data);
            Modules.registerModule(ExecutorModule.getModuleName(), executor);
        }
    }
    
    @Override
    public void open(List<String> commandParams){
        try{
            if (commandParams.contains("in")){
                if (commandParams.contains("with")){
                    // command pattern: open [file] in [location] with [program]
                    this.openFileInLocationWithProgram(
                            commandParams.get(1), 
                            commandParams.get(3),
                            commandParams.get(5));
                } else {
                    // command pattern: open [file|folder] in [location]
                    this.openFileInLocation(
                            commandParams.get(1), 
                            commandParams.get(3));
                }
            } else {
                // command pattern: open [location]
                this.openLocation(commandParams.get(1));
            }
        } catch (IndexOutOfBoundsException indexException) {
            this.ioEngine.reportError("Unrecognizable command.");
        }
    }
    
    @Override
    public void run(List<String> commandParams){
        // command pattern: run [program_1] [program_2] [program_3]...
        for(int i = 1; i < commandParams.size(); i++){
            this.system.runProgram(commandParams.get(i));
        }
    }
    
    @Override
    public void start(List<String> commandParams){
        // command pattern: start [program]
        this.runMarkedProgram("start", commandParams);       
    }
    
    @Override
    public void stop(List<String> commandParams){
        // command pattern: stop [program]
        this.runMarkedProgram("stop", commandParams);        
    }
    
    @Override
    public void call(List<String> commandNames){
        // command pattern: call [command_1] [command_2]...
        StoredExecutorCommand command;
        for(int i = 1; i < commandNames.size(); i++){
            command = this.commandsHandler.getCommand(commandNames.get(i));
            if (command != null){
                this.executeCommand(command);
            }
        }   
    }
    
    @Override
    public void newCommand(List<String> commands, String commandName){
        this.commandsHandler.newCommand(commands, commandName);
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String locationPath, String locationName){
        if (this.system.checkIfDirectoryExists(locationPath)){
            this.locationsHandler.newLocation(locationPath, locationName); 
        }          
    }
        
    @Override
    public boolean deleteCommand(String commandName){
        return this.commandsHandler.deleteCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public boolean deleteLocation(String locationName){
        return this.locationsHandler.deleteLocation(locationName);
    }  
    
    @Override
    public List<Location> getAllLocations(){
        return this.locationsHandler.getAllLocations();
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands(){
        return this.commandsHandler.getAllCommands();
    }   
    
    @Override
    public List<String> listLocationContent(String locationName){
        Location location = this.locationsHandler.getLocation(locationName);
        if (location != null){
            List<String> locationContent = this.system.getLocationContent(location);
            if (locationContent != null){
                locationContent.add(0, location.getName());
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Location> getLocations(String locationName){
        return this.locationsHandler.getLocations(locationName);
    }
    
    @Override
    public List<StoredExecutorCommand> getCommands(String commandName){
        return this.commandsHandler.getCommands(commandName);
    }
       
    private void openLocation(String locationName){
        Location location = this.locationsHandler.getLocation(locationName);
        if (location != null){
            this.system.openLocation(location);
        } 
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        Location location = this.locationsHandler.getLocation(locationName);
        if (location != null){
            this.system.openFileInLocation(targetName, location);
        }             
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        program = program.trim().toLowerCase();
        Location location = this.locationsHandler.getLocation(locationName);
        if (location != null){
            this.system.openFileInLocationWithProgram(file, location, program);
        }    
    }
    
    private void executeCommand(StoredExecutorCommand command){
        List<String> commandParams;
        for(String commandString : command.getCommands()){
            commandParams = Arrays.asList(commandString.split("\\s+"));
            switch(commandParams.get(0)){
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
                default : {
                    this.ioEngine.reportError("Unrecognizible command.");
                }
            }
        }
    }
        
    private void runMarkedProgram(String mark, List<String> commandParams){
        if (commandParams.size() == 2){
            this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.reportError("Unrecognizable command.");
        }
    }
}