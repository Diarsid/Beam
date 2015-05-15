/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.console;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.OSExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.util.ConfigReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
            showProblemMessageAndExit("Console export failure: AlreadyBoundException");
        }catch (RemoteException re){       
            showProblemMessageAndExit("Console export failure: RemoteException");
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
                showProblemMessageAndExit("Organizer already has external output!");
            } else{
                orgIO.acceptNewIOProcessor();
                orgIO.useExternalShowTaskMethod();
                console.setOrgIO(orgIO);
                console.setOsExecutor((OSExecutorIF) registry.lookup(config.getOSExecutorName()));
                console.setTaskManager((TaskManagerIF) registry.lookup(config.getTaskManagerName()));
            }
        } catch (NotBoundException e){
            System.out.println(e.getMessage());
            e.printStackTrace();            
            showProblemMessageAndExit("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Connecting to Organizer failure: RemoteException");
        }
    }
 
    private void showProblemMessageAndExit(String message){
        System.out.println();
        System.out.println(message);
        try{
            Thread.sleep(5000);
        } catch(InterruptedException e){}
        System.exit(0);
    }
}