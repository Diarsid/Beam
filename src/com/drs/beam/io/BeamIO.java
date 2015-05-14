/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.io;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.tasks.Task;
import com.drs.beam.util.ConfigReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 *
 */

public class BeamIO implements InnerIOIF, OrgIOIF {
    // Fields =============================================================================
    static private BeamIO io = new BeamIO();
    
    private ExternalIOIF externalIOEngine;

    private boolean hasExternalIOProcessor = false;
    private boolean useExternalShowTaskMethod = false;

    // Constructors =======================================================================
    public BeamIO() {
    }

    // Methods ============================================================================

    // Methods implements InnerIOIF interface ---------------------------------------------

    /*
     *
     */
    @Override
    public void showTask(Task task){
        if (hasExternalIOProcessor && useExternalShowTaskMethod){
            try{
                externalIOEngine.showTask(task);
            } catch (RemoteException e){
                resetIOtoDefault();
                nativeShowTask(task);
            }
        } else
            nativeShowTask(task);
    }

    /*
     *
     */
    @Override
    public void informAboutError(String error){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.informAboutError(error);
            } catch (RemoteException e){
                resetIOtoDefault();
                nativeInformAboutError(error);
            }
        } else
            nativeInformAboutError(error);
    }

    /*
     *
     */
    @Override
    public void inform(String info){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.informAbout(info);
            } catch (RemoteException e){
                resetIOtoDefault();
                nativeInform(info);
            }
        } else
            nativeInform(info);
    }
    
    // Private "native" application methods for IO ----------------------------------------
    /*
     *
     */
    private void nativeShowTask(Task task){
        //
    }

    /*
     *
     */
    private void nativeInform(String info){
        System.out.println(info);
    }

    /*
     *
     */
    private void nativeInformAboutError(String error){
        System.out.println(error);
    }
    
    /*
     *
     */    
    private void resetIOtoDefault(){
        hasExternalIOProcessor = false;
        useExternalShowTaskMethod = false;
        externalIOEngine = null;
    }

    // Methods implements OrgIOIF interface -----------------------------------------------

    /*
     * Intended for accepting external object implements ExternalIOIF with RMI
     * using given port and object`s name in RMI registry on given port.
     * Invoked by ExternalIOIF object to bind himself with organizer.
     */
    @Override
    public void acceptNewIOProcessor() throws RemoteException{
        try{
            ConfigReader config = ConfigReader.getReader();
            Registry registry = LocateRegistry
                    .getRegistry(config.getConsoleHost(), config.getConsolePort());
            externalIOEngine = (ExternalIOIF) registry.lookup(config.getConsoleName());
            hasExternalIOProcessor = true;
        } catch (NotBoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /*
     * Invoked by ExternalIOIF object to get information about whether organizer already has a reference on
     * external object implements ExternalIOIF
     */
    @Override
    public boolean hasExternalIOProcessor() throws RemoteException{
        try {
            externalIOEngine.isActive();
            return true;
        } catch (RemoteException | NullPointerException e){
            resetIOtoDefault();
            return false;
        }
    }

    /*
     *
     */
    @Override
    public void useExternalShowTaskMethod() throws RemoteException{
        useExternalShowTaskMethod = true;
    }

    /*
     *
     */
    @Override
    public void useNativeShowTaskMethod() throws RemoteException{
        useExternalShowTaskMethod = false;
    }     

    /* Implements method in OrganizerRemoteInterface.
     * Intended to force organizer to use his native IO if console or other external IO which was bound with this
     * organizer instance has been closed or stopped.
     * Invokes method resetIOtoDefault() in order to use BeamIO when external IO isn`t active.
     */
    @Override
    public void setDefaultIO() throws RemoteException{
        resetIOtoDefault();
    }

    /* Implements method in OrganizerRemoteInterface.
     * Terminates this JVM instance.
     * Invoked by ExternalIOIF object to stop the program.
     */
    @Override
    public void exit() throws RemoteException {
        new Thread(new Runnable(){
            @Override
            public void run() {
                System.exit(0);
            }
        }).start();
    }
    
    // Static getter methods --------------------------------------------------------------
    
    /*
    * Static method to get instance of InnerIOIF interface to perform message output 
    * from within the programm 
    */
    public static InnerIOIF getInnerIO(){
        return io;
    }
    
    /*
    * Static method to get instance of OrgIOIF interface to export it trough RMI for external
    * usage
    */
    public static OrgIOIF getRemoteIO(){
        return io;
    }
}