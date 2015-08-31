/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam;

import com.drs.beam.modules.data.DataManager;
import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.executor.Executor;
import com.drs.beam.modules.executor.ExecutorInterface;
import com.drs.beam.modules.io.RemoteAccessInterface;
import com.drs.beam.modules.tasks.TaskManagerInterface;
import com.drs.beam.modules.tasks.TaskManager;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Main application class.
 * Creates all parts of program, initializes and exports them on localhost port trough RMI.
 */
public class Beam {
    // Fields =============================================================================    
    private static InnerIOInterface innerIO;
    private static RemoteAccessInterface remoteAccess;
    private static TaskManagerInterface taskManager;
    private static ExecutorInterface executor;

    // Constructor ========================================================================
    Beam() {
    }

    // Methods ============================================================================
   
    public static void main(String[] args) {
        ConfigContainer.parseStartArgumentsIntoConfiguration(args);
        BeamIO io = new BeamIO();
        Beam.innerIO = (InnerIOInterface) io;
        Beam.remoteAccess = (RemoteAccessInterface) io;
        DataManager dataManager = new DataManager(innerIO);
        Beam.taskManager = new TaskManager(innerIO, dataManager);
        Beam.executor = new Executor(innerIO, dataManager);
        export();
        ConfigContainer.cancel();
    }
    
    private static void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{            
            int organizerPort = Integer.parseInt(ConfigContainer.getParam(ConfigParam.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            RemoteAccessInterface orgIOStub =
                    (RemoteAccessInterface) UnicastRemoteObject.exportObject(remoteAccess, organizerPort);

            ExecutorInterface osExecutorStub =
                    (ExecutorInterface) UnicastRemoteObject.exportObject(executor, organizerPort);

            TaskManagerInterface TaskManagerStub =
                    (TaskManagerInterface) UnicastRemoteObject.exportObject(taskManager, organizerPort);

            registry.bind(ConfigContainer.getParam(ConfigParam.ORG_IO_NAME), orgIOStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.EXECUTOR_NAME), osExecutorStub);
            registry.bind(ConfigContainer.getParam(ConfigParam.TASK_MANAGER_NAME), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            innerIO.informAboutException(e, true);
        }
    }      
}