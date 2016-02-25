/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.external;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.modules.tasks.TaskMessage;

/*
 * Interface describes 
 */
public interface ExternalIOInterface extends Remote {    
    
    void isActive () throws RemoteException;
    void showTask (TaskMessage task) throws RemoteException;
    
    void reportInfo (String... info) throws RemoteException; 
    void reportMessage (String... message) throws RemoteException;
    void reportError (String... error) throws RemoteException;
    void reportException (String... description) throws RemoteException;
    
    void exitExternalIO() throws RemoteException;
    
    int chooseVariants(String message, List<String> variants) throws RemoteException;
}
