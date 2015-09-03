/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.executor;

import com.drs.beam.modules.data.DataManager;
import com.drs.beam.modules.data.dao.commands.CommandsDao;
import com.drs.beam.modules.data.dao.locations.LocationsDao;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.executor.os.OS;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public class Executor implements ExecutorInterface {
    // Fields =============================================================================
    private final InnerIOInterface ioEngine;
    private final LocationsDao locationsDao;
    private final CommandsDao commandsDao;
    private final OS system;
    
    // Constructors =======================================================================
    public Executor(InnerIOInterface io, DataManager dataManager) {
        this.ioEngine = io;
        this.locationsDao = dataManager.getLocationsDao();
        this.commandsDao = dataManager.getCommandsDao();
        this.system = OS.getOS(io);
    }

    // Methods ============================================================================
    
    @Override
    public void open(List<String> commandParams) throws RemoteException{
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
            this.ioEngine.informAboutError("Unrecognizable command.", false);
        }
    }
    
    @Override
    public void run(List<String> commandParams) throws RemoteException{
        // command pattern: run [program_1] [program_2] [program_3]...
        for(int i = 1; i < commandParams.size(); i++){
            this.system.runProgram(commandParams.get(i));
        }
    }
    
    @Override
    public void start(List<String> commandParams) throws RemoteException{
        // command pattern: start [program]
        this.runMarkedProgram("start", commandParams);       
    }
    
    @Override
    public void stop(List<String> commandParams) throws RemoteException{
        // command pattern: stop [program]
        this.runMarkedProgram("stop", commandParams);        
    }
    
    @Override
    public void call(List<String> commandParams) throws RemoteException{
        // command pattern: call [command_1] [command_2]...
        for(int i = 1; i < commandParams.size(); i++){
            Map<String, List<String>> commands = 
                this.commandsDao.getCommandsByName(commandParams.get(i));
            
            if (commands.size() < 1){
                this.ioEngine.inform("Couldn`t find such command.");
            } else if (commands.size() == 1){
                for(Map.Entry<String, List<String>> entry : commands.entrySet()){
                    this.executelistOfCommands(entry.getValue());                    
                }                
            } else {         
                List<String> numberedCommandsNames = new ArrayList<>(commands.keySet());                
                int variant = this.ioEngine.resolveVariantsWithExternalIO(
                        "There are several commands:", 
                        numberedCommandsNames
                );
                this.executelistOfCommands(
                        commands.get(numberedCommandsNames.get(variant-1)));
            }
        }   
    }
    
    @Override
    public void newCommand(List<String> command, String commandName) throws RemoteException{
        for(int i = 0; i < command.size(); i++){
            String s = command.get(i).trim().toLowerCase();
            command.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        this.commandsDao.saveNewCommand(command, commandName);
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String locationPath, String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        // if given path exists and it is actually folder, not a file
        if (this.system.ifDirectoryExists(locationPath)){            
            this.locationsDao.saveNewLocation(locationPath, locationName);
        } 
    }
        
    @Override
    public boolean deleteCommand(String commandName) throws RemoteException{
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.removeCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public boolean deleteLocation(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        return this.locationsDao.removeLocation(locationName); 
    }  
    
    @Override
    public Map<String, String> getAllLocations() throws RemoteException{
        return this.locationsDao.getLocations();
    }
    
    @Override
    public Map<String, List<String>> getAllCommands() throws RemoteException{
        return this.commandsDao.getCommands();
    }   
    
    @Override
    public List<String> listLocationContent(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            List<String> locationContent = this.system.getLocationContent(locationName);
            if (locationContent != null){
                locationContent.add(0, locationName.substring(locationName.lastIndexOf("/")+1));
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public String getLocation(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        Map<String, String> foundLocations;
        if (locationName.contains("-")){
            foundLocations = this.locationsDao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            foundLocations = this.locationsDao.getLocationsByName(locationName);            
        }
        
        if (foundLocations.size() < 1){
            this.ioEngine.inform("Couldn`t find such location.");
            return "";
        } else if (foundLocations.size() == 1) {            
            return (String)foundLocations.keySet().toArray()[0] + " : " +
                    (String)foundLocations.values().toArray()[0];
        } else {
            List<String> variants = new ArrayList<>(foundLocations.keySet());
            int variant = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several locations:", 
                    variants);
            if (variant < 0){
                return "";
            } else {
                return variants.get(variant-1) + " : " + foundLocations.get(variants.get(variant-1));
            }
        }
    }
    
    @Override
    public List<String> getCommand(String commandName) throws RemoteException{
        commandName = commandName.trim().toLowerCase();
        Map<String, List<String>> commands = this.commandsDao.getCommandsByName(commandName);
        
        if (commands.size() < 1){
            this.ioEngine.inform("Couldn`t find such command");
            return new ArrayList<>();
        } else if (commands.size() == 1){
            List<String> commandContent = new ArrayList<>(
                    commands.get((String)commands.keySet().toArray()[0]));
            commandContent.add(0, (String)commands.keySet().toArray()[0]);
            return commandContent;
        } else {
            List<String> commandVariants = new ArrayList(commands.keySet());
            int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several commands:", 
                    commandVariants);
            if (varNumber < 0) {
                return new ArrayList<>();
            } else {
                List<String> commandContent = commands.get((String)commands.keySet().toArray()[varNumber-1]);
                commandContent.add(0, (String)commands.keySet().toArray()[varNumber-1]);
                return commandContent;
            }
        }
    }
       
    private void openLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: projects
        // location pattern: C:/path/to/my/projects
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            this.system.openLocation(locationName);
        } 
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: proj
        // locationName corrected into location pattern: C:/path/to/my/projects
        
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            this.system.openFileInLocation(targetName, locationName);
        }             
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        program = program.trim().toLowerCase();
        
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            this.system.openFileInLocationWithProgram(file, locationName, program);
        }    
    }
    
    private void executelistOfCommands(List<String> list){
        try{
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
                        this.ioEngine.informAboutError("Unrecognizible command.", false);
                    }
                }
            }
        } catch (RemoteException e){
            this.ioEngine.informAboutException(e, false);
        }
    }
        
    private String resolveMultipleLocationsInDB(String locationName){
        Map<String, String> foundLocations;
        if (locationName.contains("-")){
            foundLocations = this.locationsDao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            foundLocations = this.locationsDao.getLocationsByName(locationName);            
        } 
        
        if (foundLocations.size() < 1){
            this.ioEngine.inform("Couldn`t find such location.");
            return "";
        } else if (foundLocations.size() == 1){
            // If there is only one entry in the map, convert values to array 
            // and get it by index [0]
            return (String)foundLocations.values().toArray()[0];
        } else {
            List<String> locationVariants = new ArrayList(foundLocations.keySet());
            int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several locations:", 
                    locationVariants);
            if (varNumber < 0){
                return "";
            } else {
                return foundLocations.get(locationVariants.get(varNumber-1));
            }            
        }
    }
        
    private void runMarkedProgram(String mark, List<String> commandParams){
        if (commandParams.size() == 2){
            this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.informAboutError("Unrecognizable command.", false);
        }
    }
}