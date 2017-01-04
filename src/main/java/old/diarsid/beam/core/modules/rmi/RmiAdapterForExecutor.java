/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import old.diarsid.beam.core.modules.ExecutorModule;

import old.diarsid.beam.core.entities.OldBatch;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;

/**
 *
 * @author Diarsid
 */
class RmiAdapterForExecutor implements RmiExecutorInterface {
    
    private final ExecutorModule executorModule;

    RmiAdapterForExecutor(ExecutorModule executorModule) {
        this.executorModule = executorModule;
    }
    
    @Override
    public void open(List<String> commandParams) throws RemoteException {
        this.executorModule.open(commandParams);
    }
    
    @Override
    public void run(List<String> commandParams) throws RemoteException {
        this.executorModule.run(commandParams);
    }
    
    @Override
    public void call(List<String> commandParams) throws RemoteException {
        this.executorModule.call(commandParams);
    }
    
    @Override
    public void start(List<String> commandParams) throws RemoteException {
        this.executorModule.start(commandParams);
    }
    
    @Override
    public void stop(List<String> commandParams) throws RemoteException {
        this.executorModule.stop(commandParams);
    }
    
    @Override
    public void newCommand(List<String> command, String commandName) 
            throws RemoteException {
        
        this.executorModule.newBatch(command, commandName);
    }
    
    @Override
    public void openWebPage(List<String> commandParams) throws RemoteException {
        this.executorModule.openWebPage(commandParams);
    }
    
    @Override
    public List<String> listLocationContent(String locationName) 
            throws RemoteException {
        
        return this.executorModule.list(locationName);
    }
    
    @Override
    public List<OldBatch> getAllCommands() throws RemoteException {
        return this.executorModule.getAllBatches();
    }
    
    @Override
    public List<OldBatch> getCommand(String commandName) 
            throws RemoteException {
        
        return this.executorModule.getBathesByName(commandName);
    }
    
    @Override
    public boolean deleteCommand(String commandName) throws RemoteException {
        return this.executorModule.deleteBatch(commandName);
    }
    
    @Override
    public void setIntelligentActive(boolean isActive) throws RemoteException {
        this.executorModule.setIntelligentActive(isActive);
    } 
    
    @Override
    public boolean deleteMem(String command) throws RemoteException {
        return this.executorModule.deleteFromExecutorMemory(command);
    }     
    
    @Override
    public void rememberChoiceAutomatically(boolean auto) 
            throws RemoteException {        
        this.executorModule.rememberChoiceAutomatically(auto);
    } 
    
    @Override
    public Map<String, List<String>> getFromExecutorMemory(String memPattern) 
            throws RemoteException {
        return this.executorModule.getFromExecutorMemory(memPattern);
    }
    
    /*
    @Override
    public void newNote(List<String> commandParams) throws RemoteException {
        this.executorModule.newNote(commandParams);
    }
    */
    
    @Override
    public void openNotes() throws RemoteException {
        this.executorModule.openNotes();
    }
    
    @Override
    public void openNote(List<String> commandParams) throws RemoteException {
        this.executorModule.openNote(commandParams);
    }    
    
    @Override
    public void executeIfExists(List<String> commandParams) throws RemoteException {
        this.executorModule.executeIfExists(commandParams);
    }
}
