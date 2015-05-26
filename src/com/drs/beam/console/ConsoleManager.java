/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.console;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.util.config.ConfigReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Class is responsoble for exproting Console object on port 
 * through RMI and connecting it with organizer.
 */
public class ConsoleManager{
    // Fields =============================================================================
    private final ConfigReader config = ConfigReader.getReader();
    private final Console console;

    // Constructor ========================================================================
    ConsoleManager(Console console) {
        this.console = console;
    }

    // Methods ============================================================================

    void connect(){
        export();
        searchForOrganizer();
    }
    
    private void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            int port = config.getConsolePort();
            Registry registry = LocateRegistry.createRegistry(port);
            ExternalIOIF consoleStub =
                    (ExternalIOIF) UnicastRemoteObject.exportObject(console, port);
            registry.bind(config.getConsoleName(), consoleStub);
        }catch (AlreadyBoundException abe){
            showProblemMessageAndClose("Console export failure: AlreadyBoundException");
        }catch (RemoteException re){       
            showProblemMessageAndClose("Console export failure: RemoteException");
        }
    }
    
    private void searchForOrganizer() {
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            Registry registry = LocateRegistry
                    .getRegistry(config.getOrganizerHost(), config.getOrganizerPort());                
            OrgIOIF orgIO = (OrgIOIF) registry.lookup(config.getOrgIOName());
            if(orgIO.hasExternalIOProcessor()){
                showProblemMessageAndClose("Organizer already has external output!");
            } else{
                orgIO.acceptNewIOProcessor();
                orgIO.useNativeShowTaskMethod();
                console.setOrgIO(orgIO);
                console.setOsExecutor((ExecutorIF) registry.lookup(config.getOSExecutorName()));
                console.setTaskManager((TaskManagerIF) registry.lookup(config.getTaskManagerName()));
            }
        } catch (NotBoundException e){    
            showProblemMessageAndClose("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re){
            showProblemMessageAndClose("Connecting to Organizer failure: RemoteException");
        }
    }
 
    private void showProblemMessageAndClose(String message){
        try{
            console.informAboutError(message, false);
            console.close();
        } catch(RemoteException e){}
    }
}