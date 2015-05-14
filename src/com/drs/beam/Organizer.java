/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.osexec.OSExecutor;
import com.drs.beam.remote.codebase.OSExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.tasks.TaskManager;
import com.drs.beam.util.ConfigReader;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class Organizer{
    // Fields ---------------------------------------------------------------------------------
    private final InnerIOIF innerIO;
    private final OrgIOIF remoteIO;
    private final TaskManagerIF taskManager;
    private final OSExecutorIF osExecutor;

    // Constructors ---------------------------------------------------------------------------
    Organizer() {
        this.innerIO = BeamIO.getInnerIO();
        this.remoteIO = BeamIO.getRemoteIO();
        this.taskManager = new TaskManager();
        this.osExecutor = new OSExecutor();
    }

    // Methods --------------------------------------------------------------------------------

    public static void main(String[] args) {
        Organizer organizer = new Organizer();        
        organizer.export();
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

            OSExecutorIF osExecutorStub =
                    (OSExecutorIF) UnicastRemoteObject.exportObject(osExecutor, organizerPort);

            TaskManagerIF TaskManagerStub =
                    (TaskManagerIF) UnicastRemoteObject.exportObject(taskManager, organizerPort);

            registry.bind(config.getOrgIOName(), orgIOStub);
            registry.bind(config.getOSExecutorName(), osExecutorStub);
            registry.bind(config.getTaskManagerName(), TaskManagerStub);

        }catch (AlreadyBoundException abe){            
            innerIO.informAboutError(abe.getMessage());
            innerIO.informAboutError("------> stack trace :");
            for (StackTraceElement element : abe.getStackTrace()){
                innerIO.informAboutError(element.toString());
            }
            System.exit(1);
        }catch (RemoteException re){
            innerIO.informAboutError(re.getMessage());
            innerIO.informAboutError("------> stack trace :");
            for (StackTraceElement element : re.getStackTrace()){
                innerIO.informAboutError(element.toString());
            }
            System.exit(1);
        }
    }
}