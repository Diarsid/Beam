/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.entities.command.StoredExecutorCommand;
import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.data.dao.commands.CommandsDao;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.executor.os.OS;
import com.drs.beam.server.modules.io.InnerIOModule;

public class Executor implements ExecutorModule{
    // Fields =============================================================================
    private static Executor executor;
    
    private final InnerIOModule ioEngine;
    private final LocationsDao locationsDao;
    private final CommandsDao commandsDao;
    private final OS system;
    
    // Constructors =======================================================================
    private Executor(InnerIOModule io, DataManagerModule data) {
        this.ioEngine = io;
        this.locationsDao = data.getLocationsDao();
        this.commandsDao = data.getCommandsDao();
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
    public void call(List<String> commandParams){
        // command pattern: call [command_1] [command_2]...
        for(int i = 1; i < commandParams.size(); i++){
            List<StoredExecutorCommand> commands = 
                this.commandsDao.getCommandsByName(commandParams.get(i));
            
            if (commands.size() < 1){
                this.ioEngine.reportInfo("Couldn`t find such command.");
            } else if (commands.size() == 1){
                this.executelistOfCommands(commands.get(0).getCommands());
            } else {
                List<String> commandNames = new ArrayList<>();
                for (StoredExecutorCommand c : commands){
                    commandNames.add(c.getName());
                }
                int variant = this.ioEngine.resolveVariantsWithExternalIO(
                        "There are several commands:", 
                        commandNames
                );
                this.executelistOfCommands(commands.get(variant-1).getCommands());
            }
        }   
    }
    
    @Override
    public void newCommand(List<String> commands, String commandName){
        for(int i = 0; i < commands.size(); i++){
            String s = commands.get(i).trim().toLowerCase();
            commands.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        
        this.commandsDao.saveNewCommand(new StoredExecutorCommand(commandName, commands));
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String locationPath, String locationName){
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        // if given path exists and it is actually folder, not a file
        if (this.system.checkIfDirectoryExists(locationPath)){            
            this.locationsDao.saveNewLocation(new Location(locationName, locationPath));
        } 
    }
        
    @Override
    public boolean deleteCommand(String commandName){
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.removeCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public boolean deleteLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        return this.locationsDao.removeLocation(locationName); 
    }  
    
    @Override
    public List<Location> getAllLocations(){
        return this.locationsDao.getAllLocations();
    }
    
    @Override
    public List<StoredExecutorCommand> getAllCommands(){
        return this.commandsDao.getAllCommands();
    }   
    
    @Override
    public List<String> listLocationContent(String locationName){
        locationName = locationName.trim().toLowerCase();
        Location location = resolveMultipleLocationsInDB(locationName);
        if (location != null){
            List<String> locationContent = this.system.getLocationContent(location);
            if (locationContent != null){
                locationContent.add(0, location.getName());
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            this.ioEngine.reportError("No such location.");
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Location> getLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        if (locationName.contains("-")){
            return this.locationsDao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            return this.locationsDao.getLocationsByName(locationName);            
        }
    }
    
    @Override
    public List<StoredExecutorCommand> getCommand(String commandName){
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.getCommandsByName(commandName);
    }
       
    private void openLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: projects
        // location pattern: C:/path/to/my/projects
        Location location = resolveMultipleLocationsInDB(locationName);
        if (location != null){
            this.system.openLocation(location);
        } 
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: proj
        // locationName corrected into location pattern: C:/path/to/my/projects
        
        Location location = resolveMultipleLocationsInDB(locationName);
        if (location != null){
            this.system.openFileInLocation(targetName, location);
        }             
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        program = program.trim().toLowerCase();
        
        Location location = resolveMultipleLocationsInDB(locationName);
        if (location != null){
            this.system.openFileInLocationWithProgram(file, location, program);
        }    
    }
    
    private void executelistOfCommands(List<String> list){
            List<String> commandParams;
            for(String commandString : list){
                commandParams = Arrays.asList(commandString.split("\\s+"));
                switch(commandParams.get(0)){
                    case "open" :
                    case "op" :
                    case "o" : {
                        open(commandParams);
                        break;
                    } 
                    case "r" :
                    case "run" : {
                        run(commandParams);
                        break;
                    }
                    case "call" : {
                        call(commandParams);
                        break;
                    }
                    case "start" : {
                        start(commandParams);
                        break;
                    }
                    case "stop" : {
                        stop(commandParams);
                        break;
                    }
                    default : {
                        this.ioEngine.reportError("Unrecognizible command.");
                    }
                }
            }
    }
        
    private Location resolveMultipleLocationsInDB(String locationName){
        List<Location> foundLocations;
        if (locationName.contains("-")){
            foundLocations = this.locationsDao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            foundLocations = this.locationsDao.getLocationsByName(locationName);            
        } 
        
        if (foundLocations.size() < 1){
            this.ioEngine.reportInfo("Couldn`t find such location.");
            return null;
        } else if (foundLocations.size() == 1){
            return foundLocations.get(0);
        } else {
            List<String> locationNames = new ArrayList();
            for (Location loc : foundLocations){
                locationNames.add(loc.getName());
            }
            int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several locations:", 
                    locationNames);
            if (varNumber < 0){
                return null;
            } else {
                return foundLocations.get(varNumber-1);
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