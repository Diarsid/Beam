/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.io;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.modules.tasks.Task;
import com.drs.beam.modules.io.gui.Gui;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/*
 * Central class which is responsible for program`s output.
 * 
 * Defines ways to output program`s information through InnerIOInterface. This interface is
 * used by other program`s modules for informing about events, exceptions, errors, printing 
 * response information etc.
 */
public class BeamIO implements InnerIOInterface, RemoteAccessInterface {
    
    // Fields =============================================================================
    private ExternalIOInterface externalIOEngine;
    
    private final Gui gui;
    
    private boolean hasExternalIOProcessor = false;
    private boolean useExternalShowTaskMethod = false;

    // Constructors =======================================================================
    public BeamIO() {        
        this.gui = Gui.getGui();
    }

    // Methods ============================================================================
        
    /*
     * Defines way to show specified Task to user according to whether 
     * external output is available and should program uses that one or not.
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
        } else {
            nativeShowTask(task);
        }
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
        } else {
            nativeInform(info);
        }    
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
        } else {
            nativeInformAboutError(error, isCritical);
        }    
    }
    
    @Override
    public void informAboutException(Exception e, boolean isCritical){
        if (hasExternalIOProcessor){
            try {
                externalIOEngine.informAboutException(e, isCritical);
            } catch (RemoteException re) {
                nativeInformAboutException(re, isCritical);
                resetIOtoDefault();
                nativeInformAboutException(e, isCritical);
            }
        } else {
            nativeInformAboutException(e, isCritical);
        }    
    }
    
    @Override
    public int resolveVariantsWithExternalIO(String message, List<String> variants){
        int choosedVariant = 0;
        if (hasExternalIOProcessor){
            try {
                choosedVariant = externalIOEngine.chooseVariants(message, variants);
            } catch (RemoteException re) {
                resetIOtoDefault();
                choosedVariant = -2;
            }
        } else {
            choosedVariant = -2;
        }
        return choosedVariant;
    }
    
    /*
     * "Native" application methods for output.
     * Are used when external output is unavailable or program 
     * should not use it`s methods.
     */
    private void nativeShowTask(Task task){                
        gui.showTask(task);        
    }

    private void nativeInform(String info){
        gui.showMessage(info, false);
    }
    
    private void nativeInformAboutError(String error, boolean isCritical){
        gui.showMessage(error, isCritical);
    }
    
    private void nativeInformAboutException(Exception e, boolean isCritical){
        gui.showException(e, isCritical);
    }
          
    private void resetIOtoDefault(){
        hasExternalIOProcessor = false;
        useExternalShowTaskMethod = false;
        externalIOEngine = null;
    }

    /*
     * Intended for accepting external object implements ExternalIOInterface with RMI
     * using given port and object`s name in RMI registry on given port.
     * Is invoked by ExternalIOInterface object to bind himself with organizer.
     */
    @Override
    public void acceptNewIOProcessor(String consoleRmiName, String consoleHost, int consolePort) throws RemoteException{
        try{ 
            Registry registry = LocateRegistry
                    .getRegistry(consoleHost, consolePort);
            externalIOEngine = (ExternalIOInterface) registry.lookup(consoleRmiName);
            hasExternalIOProcessor = true;
        } catch (NotBoundException e){
            nativeInformAboutException(e, false);
        }
    }

    /*
     * Invoked by ExternalIOInterface object to get information about whether organizer 
     * already has a reference to external object implements ExternalIOInterface
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