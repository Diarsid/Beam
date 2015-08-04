/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.external.console;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParams;
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
            int port = Integer.parseInt(ConfigContainer.getParam(ConfigParams.CONSOLE_PORT));
            Registry registry = LocateRegistry.createRegistry(port);
            ExternalIOIF consoleStub =
                    (ExternalIOIF) UnicastRemoteObject.exportObject(console, port);
            registry.bind(ConfigContainer.getParam(ConfigParams.CONSOLE_NAME), consoleStub);
        }catch (AlreadyBoundException abe){
            System.out.println(abe.getMessage());
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
                    .getRegistry(
                            ConfigContainer.getParam(ConfigParams.ORGANIZER_HOST), 
                            Integer.parseInt(ConfigContainer.getParam(ConfigParams.ORGANIZER_PORT)));                
            OrgIOIF orgIO = (OrgIOIF) registry.lookup(ConfigContainer.getParam(ConfigParams.ORG_IO_NAME));
            if(orgIO.hasExternalIOProcessor()){
                showProblemMessageAndClose("Organizer already has external output!");
            } else{
                orgIO.acceptNewIOProcessor(
                        ConfigContainer.getParam(ConfigParams.CONSOLE_NAME), 
                        ConfigContainer.getParam(ConfigParams.CONSOLE_HOST), 
                        Integer.parseInt(ConfigContainer.getParam(ConfigParams.CONSOLE_PORT)));
                orgIO.useNativeShowTaskMethod();
                console.setOrgIO(orgIO);
                console.setTaskManager((TaskManagerIF) registry.lookup(
                        ConfigContainer.getParam(ConfigParams.TASK_MANAGER_NAME)));
                console.setOsExecutor((ExecutorIF) registry.lookup(
                        ConfigContainer.getParam(ConfigParams.EXECUTOR_NAME)));
            }
        } catch (NotBoundException e){  
            System.out.println(e.getMessage());
            showProblemMessageAndClose("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re){
            System.out.println(re.getMessage());
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