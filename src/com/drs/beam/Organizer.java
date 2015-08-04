/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam;

import com.drs.beam.modules.data.DBManager;
import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOIF;
import com.drs.beam.modules.executor.BeamExecutor;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.modules.tasks.TaskManager;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParams;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
 * Main application class.
 * Creates all parts of program, initializes and exports them on port trough RMI.
 */
public class Organizer{
    // Fields =============================================================================
    static {
        
    }
    private static InnerIOIF innerIO;
    private static OrgIOIF remoteIO;
    private static TaskManagerIF taskManager;
    private static ExecutorIF executor;

    // Constructor ========================================================================
    Organizer() {
    }

    // Methods ============================================================================

    public static void main(String[] args) {
        ConfigContainer.parseStartArgumentsIntoConfiguration(args);
        BeamIO.init();
        DBManager.init();    
        Organizer.init();
        export();
        ConfigContainer.cancel();
        System.out.println("ready!");
    }
    
    private static void init(){
        innerIO = BeamIO.getInnerIO();
        remoteIO = BeamIO.getRemoteIO();
        taskManager = new TaskManager();
        executor = new BeamExecutor();
    }
    
    private static void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{            
            int organizerPort = Integer.parseInt(
                    ConfigContainer.getParam(ConfigParams.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            OrgIOIF orgIOStub =
                    (OrgIOIF) UnicastRemoteObject.exportObject(remoteIO, organizerPort);

            ExecutorIF osExecutorStub =
                    (ExecutorIF) UnicastRemoteObject.exportObject(executor, organizerPort);

            TaskManagerIF TaskManagerStub =
                    (TaskManagerIF) UnicastRemoteObject.exportObject(taskManager, organizerPort);

            registry.bind(ConfigContainer.getParam(ConfigParams.ORG_IO_NAME), orgIOStub);
            registry.bind(ConfigContainer.getParam(ConfigParams.EXECUTOR_NAME), osExecutorStub);
            registry.bind(ConfigContainer.getParam(ConfigParams.TASK_MANAGER_NAME), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            innerIO.informAboutException(e, true);
        }
    }  
    
}