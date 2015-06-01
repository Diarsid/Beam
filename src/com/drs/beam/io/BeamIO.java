/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.io;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.tasks.Task;
import com.drs.beam.io.jfxgui.Gui;
import com.drs.beam.io.jfxgui.GuiEngine;
import com.drs.beam.util.config.ConfigReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * Central class which is responsible for program`s output.
 * Defines ways to output information and connects entire program 
 * with external output mechanism which implements ExternalIOIF e.g. 
 * external Console.
 */
public class BeamIO implements InnerIOIF, OrgIOIF {
    // Fields =============================================================================
    private static final BeamIO io = new BeamIO();
    
    private final Gui gui;
    private ExternalIOIF externalIOEngine;

    private boolean hasExternalIOProcessor = false;
    private boolean useExternalShowTaskMethod = false;

    // Constructors =======================================================================
    public BeamIO() {
        gui = new GuiEngine();
        new Thread(gui, "JavaFX Application Thread").start();
    }

    // Methods ============================================================================    
    
    public static InnerIOIF getInnerIO(){
        return io;
    }
    
    public static OrgIOIF getRemoteIO(){
        return io;
    }
    
    /*
     * Methods implements InnerIOIF interface.
     * Define way to show specified Task to user according to whether 
     * external output availability and should program uses external output or not.
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

    @Override
    public void informAboutError(String error, boolean isCritical){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.informAboutError(error, isCritical);
            } catch (RemoteException e){
                resetIOtoDefault();
                nativeInformAboutError(error, isCritical);
            }
        } else
            nativeInformAboutError(error, isCritical);
    }
    
    @Override
    public void informAboutException(Exception e, boolean isCritical){
        if (hasExternalIOProcessor){
            try {
                externalIOEngine.informAboutException(e, isCritical);
            } catch (RemoteException re) {
                resetIOtoDefault();
                nativeInformAboutException(e, isCritical);
            }
        } else
            nativeInformAboutException(e, isCritical);
    }
    
    /*
     * Private "native" application methods for output.
     * Are used when external output is unavailable or program 
     * should not use it`s methods.
     */
    private void nativeShowTask(Task task){
        gui.showTask(task);
    }

    private void nativeInform(String info){
        //
    }
    
    private void nativeInformAboutError(String error, boolean isCritical){
        System.out.println(error);
        if(isCritical) 
            System.exit(1);
    }
    
    private void nativeInformAboutException(Exception e, boolean isCritical){
        System.out.println(e.getMessage());
        e.printStackTrace();
        if(isCritical) 
            System.exit(1);
    }
          
    private void resetIOtoDefault(){
        hasExternalIOProcessor = false;
        useExternalShowTaskMethod = false;
        externalIOEngine = null;
    }

    /*
     * Intended for accepting external object implements ExternalIOIF with RMI
     * using given port and object`s name in RMI registry on given port.
     * Is invoked by ExternalIOIF object to bind himself with organizer.
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
            nativeInformAboutException(e, false);
        }
    }

    /*
     * Invoked by ExternalIOIF object to get information about whether organizer 
     * already has a reference to external object implements ExternalIOIF
     */
    @Override
    public boolean hasExternalIOProcessor() throws RemoteException{
        if (externalIOEngine == null){
            return false;
        } else{
            try {            
            externalIOEngine.isActive();
            return true;
            } catch (RemoteException e){
                resetIOtoDefault();
                return false;
            }            
        }        
    }
    
    @Override
    public void useExternalShowTaskMethod() throws RemoteException{
        useExternalShowTaskMethod = true;
    }
    
    @Override
    public void useNativeShowTaskMethod() throws RemoteException{
        useExternalShowTaskMethod = false;
    }     

    @Override
    public void setDefaultIO() throws RemoteException{
        resetIOtoDefault();
    }
    
    @Override
    public void exit() throws RemoteException {
        new Thread(new Runnable(){
            @Override
            public void run() {
                System.exit(0);
            }
        }).start();
    }
}