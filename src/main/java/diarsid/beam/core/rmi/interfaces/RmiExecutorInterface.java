/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.modules.executor.StoredExecutorCommand;

/*
 * 
 */
public interface RmiExecutorInterface extends Remote {
    
    void open(List<String> commandParams) throws RemoteException;
    void run(List<String> commandParams) throws RemoteException;
    void call(List<String> commandParams) throws RemoteException;
    void start(List<String> commandParams) throws RemoteException;
    void stop(List<String> commandParams) throws RemoteException;
    void openWebPage(List<String> commandParams) throws RemoteException;
    
    void newCommand(List<String> command, String commandName) throws RemoteException;
    
    List<String> listLocationContent(String locationName) throws RemoteException;
    
    List<StoredExecutorCommand> getAllCommands() throws RemoteException;    
    
    List<StoredExecutorCommand> getCommand(String commandName)  throws RemoteException;
    
    boolean deleteCommand(String commandName) throws RemoteException;
    
    void setIntelligentActive(boolean isActive) throws RemoteException;  
    boolean deleteMem(String command) throws RemoteException;    
    void rememberChoiceAutomatically(boolean auto) throws RemoteException; 
    List<String> getAllChoices() throws RemoteException;
    
    //void newNote(List<String> commandParams) throws RemoteException;
    void openNotes() throws RemoteException;
    void openNote(List<String> commandParams) throws RemoteException;
}
