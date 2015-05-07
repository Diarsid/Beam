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

/**
 * Org by Diarsid
 * Time: 15:05 - 10.01.15
 * IDE: IntelliJ IDEA 12
 */

public class ConsoleManager{
    // Fields ----------------------------------------------------------------------------------------------------------
    private Registry registry;
    private final ConfigReader config = ConfigReader.getReader();
    private final Console console;

    // Constructors ----------------------------------------------------------------------------------------------------
    ConsoleManager(Console console) {
        this.console = console;
    }

    // Methods =========================================================================================================


    void connect(){
        prepareRegistry();
        export();
        searchForOrganizer();
    }

    // Methods aocessing ----------------------------------------------------------------
    private void prepareRegistry(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try {
            registry = LocateRegistry.createRegistry(config.getConsolePort());
        }catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Console export failure: RemoteException");
        }
    }

    private void export(){
        try{
            ExternalIOIF consoleStub =
                    (ExternalIOIF) UnicastRemoteObject.exportObject(console, config.getConsolePort());
            registry.bind(config.getConsoleName(), consoleStub);
        }catch (AlreadyBoundException abe){
            //abe.printStackTrace();
            //System.out.println(abe.getMessage());
            showProblemMessageAndExit("Console export failure: AlreadyBoundException");
        }catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Console export failure: RemoteException");
        }
    }
    private void searchForOrganizer() {
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            registry = LocateRegistry
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
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            showProblemMessageAndExit("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Connecting to Organizer failure: RemoteException");
        }
    }
 
     /*   
    void connect(){
        prepareRegistry();
        export();
        searchForOrganizer();
    }

    // Methods aocessing ----------------------------------------------------------------
    private void prepareRegistry(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try {
            registry = LocateRegistry.createRegistry(23102);
        }catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Console export failure: RemoteException");
        }
    }

    private void export(){
        try{
            ExternalIOIF consoleStub =
                    (ExternalIOIF) UnicastRemoteObject.exportObject(this, 23102);
            registry.bind("console", consoleStub);
        }catch (AlreadyBoundException abe){
            //abe.printStackTrace();
            //System.out.println(abe.getMessage());
            showProblemMessageAndExit("Console export failure: AlreadyBoundException");
        }catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Console export failure: RemoteException");
        }
    }
    private void searchForOrganizer() {
        try{
            registry = LocateRegistry
                    .getRegistry("127.0.0.1", 23101);

            OrgIOIF orgIO = (OrgIOIF) registry.lookup("orgIO");
            if(orgIO.hasExternalIOProcessor()){
                showProblemMessageAndExit("Organizer already has external output!");
            } else{
                orgIO.acceptNewIOProcessor();
                console.setOrgIO(orgIO);
                console.setOsExecutor((OSExecutorIF) registry.lookup("OSExecutor"));
                console.setTaskManager((TaskManagerIF) registry.lookup("TaskManager"));
            }
        } catch (NotBoundException e){
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            showProblemMessageAndExit("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re){
            //re.printStackTrace();
            //System.out.println(re.getMessage());
            showProblemMessageAndExit("Connecting to Organizer failure: RemoteException");
        }
    }
    */ 
    private void showProblemMessageAndExit(String message){
        System.out.println();
        System.out.println(message);
        try{
            Thread.sleep(5000);
        } catch(InterruptedException e){}
        System.exit(0);
    }
}