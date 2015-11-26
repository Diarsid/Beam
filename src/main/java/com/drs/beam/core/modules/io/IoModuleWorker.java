/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.io;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.core.modules.IoModule;

/*
 * Central class which is responsible for program`s output.
 * 
 * Defines ways to output program`s information through InnerIOModule. This interface is
 * used by other program`s modules for informing about events, exceptions, errors, printing 
 * response information etc.
 */
class IoModuleWorker implements IoModule {
    
    private ExternalIOInterface externalIOEngine;
    private boolean hasExternalIOProcessor;
    private boolean useExternalShowTaskMethod;
    
    IoModuleWorker() {
        this.hasExternalIOProcessor = false;
        this.useExternalShowTaskMethod = false;
    }
    
    
    @Override
    public ExternalIOInterface getExternalIOEngine(){
        return this.externalIOEngine;
    }
    
    @Override      
    public void resetIoToDefault(){
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
    public void acceptNewExternalIOProcessor(
            String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException{
        
        Registry registry = 
                LocateRegistry.getRegistry(consoleHost, consolePort);
        ExternalIOInterface externalIO = 
                (ExternalIOInterface) registry.lookup(consoleRmiName);
        this.externalIOEngine = externalIO;
        this.hasExternalIOProcessor = true;
    }

    /*
     * Invoked by ExternalIOInterface object to get information about whether organizer 
     * already has a reference to external object implements ExternalIOInterface
     */
    @Override
    public boolean hasExternalIOProcessor(){
        return this.hasExternalIOProcessor;
    }
    
    @Override
    public boolean useExternalShowTaskMethod(){
        return this.useExternalShowTaskMethod;
    }
    
    @Override
    public void setUseExternalShowTaskMethod(){
        useExternalShowTaskMethod = true;
    }
    
    @Override
    public void setUseNativeShowTaskMethod(){
        useExternalShowTaskMethod = false;
    } 
    
    @Override
    public void exitBeam(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                System.exit(0);
            }
        }).start();
    }
    
    @Override
    public boolean isExternalProcessorActive() {
        try {            
            externalIOEngine.isActive();
            return true;
        } catch (RemoteException|NullPointerException e){
            this.resetIoToDefault();
            return false;
        }
    }
}