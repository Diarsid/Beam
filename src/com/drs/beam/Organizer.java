/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.io.jfxgui.GuiEngine;
import com.drs.beam.io.jfxgui.Gui;
import com.drs.beam.executor.Executor;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.tasks.TaskManager;
import com.drs.beam.util.config.ConfigReader;
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
    private final InnerIOIF innerIO;
    private final OrgIOIF remoteIO;
    private final TaskManagerIF taskManager;
    private final ExecutorIF osExecutor;

    // Constructor ========================================================================
    Organizer() {
        this.innerIO = BeamIO.getInnerIO();
        this.remoteIO = BeamIO.getRemoteIO();        
        this.osExecutor = new Executor();
        this.taskManager = new TaskManager();
    }

    // Methods ============================================================================

    public static void main(String[] args) {
        Organizer organizer = new Organizer();        
        organizer.export();
        System.out.println("ready!");
    }
    
    private void export(){
        if (System.getSecurityManager()==null)
            System.setSecurityManager(new SecurityManager());
        try{
            ConfigReader config = ConfigReader.getReader();
            int organizerPort = config.getOrganizerPort();
            Registry registry = LocateRegistry.createRegistry(organizerPort);
            OrgIOIF orgIOStub =
                    (OrgIOIF) UnicastRemoteObject.exportObject(remoteIO, organizerPort);

            ExecutorIF osExecutorStub =
                    (ExecutorIF) UnicastRemoteObject.exportObject(osExecutor, organizerPort);

            TaskManagerIF TaskManagerStub =
                    (TaskManagerIF) UnicastRemoteObject.exportObject(taskManager, organizerPort);

            registry.bind(config.getOrgIOName(), orgIOStub);
            registry.bind(config.getOSExecutorName(), osExecutorStub);
            registry.bind(config.getTaskManagerName(), TaskManagerStub);

        }catch (AlreadyBoundException|RemoteException e){            
            innerIO.informAboutException(e, true);
        }
    }
}