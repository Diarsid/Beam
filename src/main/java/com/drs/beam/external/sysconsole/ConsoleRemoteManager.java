/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.external.sysconsole;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/*
 * Class is responsoble for exproting Console object on port 
 * through RMI and connecting it with organizer.
 */
public class ConsoleRemoteManager{
    // Fields =============================================================================
    private final Console console;

    // Constructor ========================================================================
    ConsoleRemoteManager(Console console) {
        this.console = console;
    }

    // Methods ============================================================================

    void connect(){
        export();
        searchForBeamServer();
    }
    
    private void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            int port = Integer.parseInt(ConfigContainer.getParam(ConfigParam.CONSOLE_PORT));
            Registry registry = LocateRegistry.createRegistry(port);
            ExternalIOInterface consoleStub =
                    (ExternalIOInterface) UnicastRemoteObject.exportObject(console, port);
            registry.bind(ConfigContainer.getParam(ConfigParam.CONSOLE_NAME), consoleStub);
        }catch (AlreadyBoundException abe){
            System.out.println(abe.getMessage());
            showProblemMessageAndClose("Console export failure: AlreadyBoundException");
        }catch (RemoteException re){       
            showProblemMessageAndClose("Console export failure: RemoteException");
        }
    }
    
    private void searchForBeamServer() {
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            Registry registry = LocateRegistry
                    .getRegistry(ConfigContainer.getParam(ConfigParam.BEAMCORE_HOST), 
                            Integer.parseInt(ConfigContainer.getParam(ConfigParam.BEAMCORE_PORT)));                
            RmiRemoteControlInterface remoteAccessInterface = (RmiRemoteControlInterface) registry.lookup(ConfigContainer.getParam(ConfigParam.BEAM_ACCESS_NAME));
            if(remoteAccessInterface.isExternalIoProcessorActive()){
                showProblemMessageAndClose("Organizer already has external output!");
            } else{
                remoteAccessInterface.acceptNewIOProcessor(ConfigContainer.getParam(ConfigParam.CONSOLE_NAME), 
                        ConfigContainer.getParam(ConfigParam.CONSOLE_HOST), 
                        Integer.parseInt(ConfigContainer.getParam(ConfigParam.CONSOLE_PORT)));
                remoteAccessInterface.useNativeShowTaskMethod();
                console.setBeamRemoteAccess(remoteAccessInterface);
                console.setTaskManager((RmiTaskManagerInterface) registry.lookup(ConfigContainer.getParam(ConfigParam.TASK_MANAGER_NAME)));
                console.setExecutor((RmiExecutorInterface) registry.lookup(ConfigContainer.getParam(ConfigParam.EXECUTOR_NAME)));
                console.setLocationsHandler((RmiLocationsHandlerInterface) registry.lookup(ConfigContainer.getParam(ConfigParam.LOCATIONS_HANDLER_NAME)));
                console.setWebPagesHandler((RmiWebPageHandlerInterface) registry.lookup(ConfigContainer.getParam(ConfigParam.WEB_PAGES_HANDLER_NAME)));
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
            console.reportError(message);
            console.closeConsole();
        } catch(RemoteException e){}
    }
}