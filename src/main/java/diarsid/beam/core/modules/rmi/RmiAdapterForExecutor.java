/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.modules.ExecutorModule;
import diarsid.beam.core.modules.executor.entities.StoredCommandsBatch;
import diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;

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
        
        this.executorModule.newCommand(command, commandName);
    }
    
    @Override
    public void openWebPage(List<String> commandParams) throws RemoteException {
        this.executorModule.openWebPage(commandParams);
    }
    
    @Override
    public List<String> listLocationContent(String locationName) 
            throws RemoteException {
        
        return this.executorModule.listLocationContent(locationName);
    }
    
    @Override
    public List<StoredCommandsBatch> getAllCommands() throws RemoteException {
        return this.executorModule.getAllCommands();
    }
    
    @Override
    public List<StoredCommandsBatch> getCommand(String commandName) 
            throws RemoteException {
        
        return this.executorModule.getCommands(commandName);
    }
    
    @Override
    public boolean deleteCommand(String commandName) throws RemoteException {
        return this.executorModule.deleteCommand(commandName);
    }
    
    @Override
    public void setIntelligentActive(boolean isActive) throws RemoteException {
        this.executorModule.setIntelligentActive(isActive);
    } 
    
    @Override
    public boolean deleteMem(String command) throws RemoteException {
        return this.executorModule.deleteMem(command);
    }     
    
    @Override
    public void rememberChoiceAutomatically(boolean auto) 
            throws RemoteException {        
        this.executorModule.rememberChoiceAutomatically(auto);
    } 
    
    @Override
    public List<String> getAllChoices() throws RemoteException {
        return this.executorModule.getAllChoices();
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
