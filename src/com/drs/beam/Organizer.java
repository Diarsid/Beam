/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam;

import com.drs.beam.modules.Module;
import com.drs.beam.modules.ModuleName;
import com.drs.beam.modules.data.DataManager;
import com.drs.beam.modules.io.BeamIO;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.executor.BeamExecutor;
import com.drs.beam.remote.codebase.ExecutorInterface;
import com.drs.beam.remote.codebase.RemoteAccessInterface;
import com.drs.beam.modules.tasks.TaskManagerInterface;
import com.drs.beam.modules.tasks.TaskManager;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/*
 * Main application class.
 * Creates all parts of program, initializes and exports them on port trough RMI.
 */
public class Organizer{
    // Fields =============================================================================    
    private static InnerIOInterface innerIO;
    private static RemoteAccessInterface remoteIO;
    private static TaskManagerInterface taskManager;
    private static ExecutorInterface executor;
    
    private static final Map<ModuleName, Module> modules = new HashMap<>();

    // Constructor ========================================================================
    Organizer() {
    }

    // Methods ============================================================================
    
    public static Module getModule(ModuleName name){
        return modules.get(name);        
    }
    
    private static void register(Module module, ModuleName name){
        modules.put(name, module);
    }
    
    private static void unregister(ModuleName name){
        modules.remove(name);
    }

    public static void main(String[] args) {
        ConfigContainer.parseStartArgumentsIntoConfiguration(args);
        BeamIO.init();
        DataManager dataManager = new DataManager(innerIo);
        dataManager.init();
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
            int organizerPort = Integer.parseInt(ConfigContainer.getParam(ConfigParam.ORGANIZER_PORT));
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            RemoteAccessInterface orgIOStub =
                    (RemoteAccessInterface) UnicastRemoteObject.exportObject(remoteIO, organizerPort);

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