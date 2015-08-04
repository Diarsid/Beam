/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.executor;

import com.drs.beam.modules.data.dao.executor.ExecutorDao;
import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.modules.executor.os.OS;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class BeamExecutor implements ExecutorIF {
    // Fields =============================================================================
    private final InnerIOIF ioEngine;
    private final ExecutorDao dao;
    private final OS system;
    
    // Constructors =======================================================================
    public BeamExecutor() {
        this.ioEngine = BeamIO.getInnerIO();
        this.dao = ExecutorDao.getDao();        
        this.system = OS.getOS();
    }

    // Methods ============================================================================
    @Override
    public void open(String command) throws RemoteException{
        List<String> commandParts = prepareCommand(command);
        try{
            if (commandParts.contains("in")){
                if (commandParts.contains("with")){
                    // command pattern: open [file] in [location] with [program]
                    this.openFileInLocationWithProgram(
                            commandParts.get(1), 
                            commandParts.get(3),
                            commandParts.get(5));
                } else {
                    // command pattern: open [file|folder] in [location]
                    this.openFileInLocation(
                            commandParts.get(1), 
                            commandParts.get(3));
                }
            } else {
                // command pattern: open [location]
                this.openLocation(commandParts.get(1));
            }
        } catch (IndexOutOfBoundsException indexException) {
            ioEngine.informAboutError("Unrecognizable command.", false);
        }
    }
    
    @Override
    public void run(String command) throws RemoteException{
        List<String> commandParts = prepareCommand(command);
        // command pattern: run [program]...
        this.runGivenPrograms(commandParts);
    }
    
    @Override
    public void start(String command) throws RemoteException{
        runMarkedProgram("start", command);       
    }
    
    @Override
    public void stop(String command) throws RemoteException{
        runMarkedProgram("stop", command);        
    }
    
    @Override
    public void call(String command) throws RemoteException{
        List<String> commandParts = prepareCommand(command);
        // command pattern: call [command]...
        this.callGivenCommands(commandParts);
    }
    
    @Override
    public void newCommand(List<String> command, String commandName) throws RemoteException{
        for(int i = 0; i < command.size(); i++){
            String s = command.get(i).trim().toLowerCase();
            command.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        dao.saveNewCommand(command, commandName);
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String locationPath, String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        // if given path exists and it is actually folder, not a file
        if (system.ifDirectoryExists(locationPath)){            
            dao.saveNewLocation(locationPath, locationName);
        } 
    }
        
    @Override
    public boolean deleteCommand(String commandName) throws RemoteException{
        commandName = commandName.trim().toLowerCase();
        return dao.removeCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public boolean deleteLocation(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        return dao.removeLocation(locationName); 
    }  
    
    @Override
    public Map<String, String> getLocations() throws RemoteException{
        return dao.getLocations();
    }
    
    @Override
    public Map<String, List<String>> getCommands() throws RemoteException{
        return dao.getCommands();
    }    
       
    private void openLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: projects
        // location pattern: C:/path/to/my/projects
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            system.openLocation(locationName);
        } 
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: proj
        // locationName corrected into location pattern: C:/path/to/my/
        
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            system.openFileInLocation(targetName, locationName);
        }             
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        program = program.trim().toLowerCase();
        
        locationName = resolveMultipleLocationsInDB(locationName);
        if (locationName.length() > 0){
            system.openFileInLocationWithProgram(file, locationName, program);
        }    
    }
    
    private void runGivenPrograms(List<String> arguments){
        for(int i = 1; i < arguments.size(); i++){
            system.runProgram(arguments.get(i).trim().toLowerCase());
        }
    }
    
    private void callGivenCommands(List<String> arguments){
        for(int i = 1; i < arguments.size(); i++){
            Map<String, List<String>> commands = 
                dao.getCommandsByName(arguments.get(i));
            
            if (commands.size() < 1){
                ioEngine.inform("Couldn`t find such command.");
            } else if (commands.size() == 1){
                for(Map.Entry<String, List<String>> entry : commands.entrySet()){
                    executelistOfCommands(entry.getValue());                    
                }                
            } else {         
                List<String> numberedCommandsNames = new ArrayList<>();
                for (Entry<String, List<String>> entry : commands.entrySet()) {
                    numberedCommandsNames.add(entry.getKey());   
                }
                int variant = ioEngine.resolveVariantsWithExternalIO(
                        "There are several commands:", 
                        numberedCommandsNames
                );
                executelistOfCommands(
                        commands.get(numberedCommandsNames.get(variant-1)));
            }
        }           
    }
    
    private void executelistOfCommands(List<String> list){
        try{
            for(String command : list){
                if (command.startsWith("open")){
                    open(command);
                } else if (command.startsWith("run")){
                    run(command);
                } else if (command.startsWith("call")){
                    call(command);
                }
            }
        } catch (RemoteException e){
            ioEngine.informAboutException(e, false);
        }
    }
    
    private String resolveMultipleLocationsInDB(String locationName){
        Map<String, String> foundedLocations;
        if (locationName.contains("-")){
            foundedLocations = dao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            foundedLocations = dao.getLocationsByName(locationName);            
        } 
        
        if (foundedLocations.size() < 1){
            ioEngine.inform("Couldn`t find such location.");
            return "";
        } else if (foundedLocations.size() == 1){
            // If there is only one entry in the map, convert values to array 
            // and get it by index [0]
            return (String)foundedLocations.values().toArray()[0];
        } else {
            List<String> locationVariants = new ArrayList(foundedLocations.keySet());
            int varNumber = ioEngine.resolveVariantsWithExternalIO(
                    "There are several locations:", 
                    locationVariants);
            if (varNumber < 0){
                return "";
            } else {
                return foundedLocations.get(locationVariants.get(varNumber-1));
            }            
        }
    }
    
    private List<String> prepareCommand(String command){
        ArrayList<String> commmandParts = new ArrayList<>(
                Arrays.asList(command.trim().toLowerCase().split(" ")));
        commmandParts.remove("");
        return commmandParts;
    }
    
    private void runMarkedProgram(String mark, String command){
        List<String> commandParts = prepareCommand(command);
        if (commandParts.size() == 2){
            system.runProgram(commandParts.get(1)+"-"+mark);
        } else {
            ioEngine.informAboutError("Unrecognizable command.", false);
        }
    }
}