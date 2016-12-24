/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.modules.io;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import diarsid.beam.core.Beam;

import old.diarsid.beam.external.ExternalIOInterface;
import old.diarsid.beam.core.modules.OldIoModule;

/*
 * Central class which is responsible for a program`s output.
 * 
 * Defines ways to output program`s information through InnerIOModule. This interface is
 * used by other program`s modules for informing about events, exceptions, errors, printing 
 * response information etc.
 */
class OldIoModuleWorker implements OldIoModule {
    
    private ExternalIOInterface externalIOEngine;
    private boolean hasExternalIOProcessor;
    private boolean useExternalShowTaskMethod;
    
    OldIoModuleWorker() {
        this.hasExternalIOProcessor = false;
        this.useExternalShowTaskMethod = false;
    }
    
    @Override
    public void stopModule() {
        try {
            this.externalIOEngine.exitExternalIO();            
        } catch (RemoteException e) {
            this.resetIoToDefault();
        }
    }
    
    @Override
    public ExternalIOInterface getExternalIOEngine() {
        return this.externalIOEngine;
    }
    
    @Override      
    public void resetIoToDefault() {
        this.hasExternalIOProcessor = false;
        this.useExternalShowTaskMethod = false;
        this.externalIOEngine = null;
    }
    
    /*
     * Intended for accepting external object implements ExternalIOInterface with RMI
     * using given port and object`s name in RMI registry on given port.
     * Is invoked by ExternalIOInterface object to bind himself with organizer.
     */
    @Override
    public void acceptNewExternalIOProcessor(
            String consoleRmiName, String consoleHost, int consolePort) 
            throws RemoteException, NotBoundException {
        
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
    public boolean hasExternalIOProcessor() {
        return this.hasExternalIOProcessor;
    }
    
    @Override
    public boolean useExternalShowTaskMethod() {        
        return this.useExternalShowTaskMethod;
    }
    
    @Override
    public boolean setUseExternalShowTaskMethod() {
        this.useExternalShowTaskMethod = true;
        return true;
    }
    
    @Override
    public boolean setUseNativeShowTaskMethod() {
        this.useExternalShowTaskMethod = false;
        return true;
    } 
    
    @Override
    public void exitBeam() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Beam.exitBeamCoreNow();
            }
        }).start();
    }
    
    @Override
    public boolean isExternalProcessorActive() {
        try {            
            this.externalIOEngine.isActive();
            return true;
        } catch (RemoteException|NullPointerException e) {
            this.resetIoToDefault();
            return false;
        }
    }    
}