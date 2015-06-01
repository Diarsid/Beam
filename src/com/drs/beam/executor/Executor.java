/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.executor;

import com.drs.beam.executor.dao.ExecutorDao;
import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.executor.os.OS;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public class Executor implements ExecutorIF {
    // Fields =============================================================================
    private final InnerIOIF ioEngine;
    private final ExecutorDao dao;
    private final OS system;
    
    // Constructors =======================================================================
    public Executor() {
        this.ioEngine = BeamIO.getInnerIO();
        this.dao = ExecutorDao.getDao();        
        this.system = OS.getOS(dao.getLocationByName("programs"));
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
        this.runGivemPrograms(commandParts);
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
    // location pattern: C:/path/to/target/folder
    public void newLocation(String location) throws RemoteException{
        location = location.trim().toLowerCase();
    }
    
    @Override
    public void deleteCommand(String commandName) throws RemoteException{
        commandName = commandName.trim().toLowerCase();
        dao.removeCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public void deleteLocation(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        dao.removeLocation(locationName); 
    }  
    
    @Override
    public Map<String, String> viewLocations() throws RemoteException{
        return dao.viewLocations();
    }
    
    @Override
    public Map<String, List<String>> viewCommands() throws RemoteException{
        return dao.viewCommands();
    }    
       
    private void openLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: projects
        // location pattern: C:/path/to/my/projects
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openLocation(location);
        }        
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        // locationName pattern: proj
        // locationName corrected into location pattern: C:/path/to/my/
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openFileInLocation(targetName, location);
        }               
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        locationName = locationName.trim().toLowerCase();
        program = program.trim().toLowerCase();
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openFileInLocationWithProgram(file, location, program);
        }    
    }
    
    private void runGivemPrograms(List<String> arguments){
        for(int i = 1; i < arguments.size(); i++){
            system.runProgram(arguments.get(i).trim().toLowerCase());
        }
    }
    
    private void callGivenCommands(List<String> arguments){
        List<String> commands = dao.getCommandsByNames(arguments.subList(1, arguments.size()));
        if (commands.size() > 0){
            try{
                for(String command : commands){
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
    }
    
    private List<String> prepareCommand(String command){
        command = command.trim().toLowerCase();
        return Arrays.asList(command.split(" "));
    }
}