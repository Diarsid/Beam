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


public class Executor implements ExecutorIF {
    // Fields =============================================================================
    private final InnerIOIF ioEngine;
    private final ExecutorDao dao;
    private final OS system;
    private List<String> commandParts;    
    
    // Syntactical commands parts.
    private final String CALL = "call";
    private final String RUN = "run";
    private final String OPEN = "open";
    private final String IN = "in";
    private final String WITH = "with";

    // Constructors =======================================================================
    public Executor() {
        this.ioEngine = BeamIO.getInnerIO();
        this.dao = ExecutorDao.getDao();        
        this.system = OS.getOS(dao.getLocationByName("programs"));
    }

    // Methods ============================================================================
    @Override
    public void execute(String command) throws RemoteException{
        this.doWork(command);
    }
    
    @Override
    public void newCommand(List<String> command, String commandName) throws RemoteException{
        dao.saveNewCommand(command, commandName);
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String locationPath, String locationName) throws RemoteException{
        // if given path exists and it is actually folder, not a file
        if (system.ifDirectoryExists(location)){
            
            dao.saveNewLocation(location);
        } 
    }
    
    @Override
    // location pattern: C:/path/to/target/folder
    public void newLocation(String location) throws RemoteException{
        
    }
    
    @Override
    public void deleteCommand(String commandName) throws RemoteException{
        dao.removeCommand(commandName);
    }
    
    @Override
    // location pattern: projects
    public void deleteLocation(String locationName) throws RemoteException{
        dao.removeLocation(locationName); 
    }  
    
    private void doWork(String command){
        command = command.trim();
        commandParts = Arrays.asList(command.split(" "));
        if (commandParts.size() > 0){
            try {
                choosing: switch (commandParts.get(0)){
                    // command pattern: open [arguments]...
                    case(OPEN) : {
                        if (commandParts.contains(IN)){
                            if (commandParts.contains(WITH)){
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
                        break choosing;
                    }
                    case(RUN) : {
                        // command pattern: run [program]...
                        this.run(commandParts);
                        break choosing;
                    }
                    case(CALL) : {
                        // command pattern: call [command]...
                        this.call(commandParts);
                        break choosing;
                    }
                    default : {
                        ioEngine.inform("'" + commandParts.get(0) + "' is unknown command.");
                    }
                }
            } catch (IndexOutOfBoundsException indexException) {
                ioEngine.informAboutError("Unrecognizable command.", false);
            }
        }            
        commandParts = null;
    }
    
    private void openLocation(String locationName){
        // locationName pattern: projects
        // location pattern: C:/path/to/my/projects
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openLocation(location);
        }        
    }
    
    private void openFileInLocation(String targetName, String locationName){        
        // locationName pattern: proj
        // locationName corrected into location pattern: C:/path/to/my/
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openFileInLocation(targetName, location);
        }               
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        String location = dao.getLocationByName(locationName);
        if (location.length() > 0){
            system.openFileInLocationWithProgram(file, location, program);
        }    
    }
    
    private void run(List<String> arguments){
        for(int i = 1; i < arguments.size(); i++){
            system.runProgram(arguments.get(i));
        }
    }
    
    private void call(List<String> arguments){
        List<String> commands = dao.getCommandsByNames(arguments.subList(1, arguments.size()));
        if (commands != null){
            for(String command : commands){
                this.doWork(command);
            }
        }                
    }
}