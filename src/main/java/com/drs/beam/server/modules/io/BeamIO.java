/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.io;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.io.gui.Gui;
import com.drs.beam.server.modules.io.gui.GuiJavaFX;

/*
 * Central class which is responsible for program`s output.
 * 
 * Defines ways to output program`s information through InnerIOModule. This interface is
 * used by other program`s modules for informing about events, exceptions, errors, printing 
 * response information etc.
 */
public class BeamIO implements InnerIOModule, RemoteControlModule, InnerControlModule {
    
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private ExternalIOInterface externalIOEngine;    
    private final Gui gui;    
    private boolean hasExternalIOProcessor;
    private boolean useExternalShowTaskMethod;

// ________________________________________________________________________________________
//                                     Constructor                                         
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    public BeamIO() {        
        this.gui = GuiJavaFX.buildAndLaunchGui();
        this.hasExternalIOProcessor = false;
        this.useExternalShowTaskMethod = false;
    }

// ________________________________________________________________________________________
//                                        Methods                                          
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
                
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
                this.resetIOtoDefault();
                this.nativeShowTask(task);
            }
        } else {
            this.nativeShowTask(task);
        }
    }
    
    @Override
    public void reportInfo(String... info){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.reportInfo(info);
            } catch (RemoteException e){
                this.resetIOtoDefault();
                this.nativeReportMessage(info);
            }
        } else {
            this.nativeReportMessage(info);
        }    
    }
    
    @Override
    public void reportMessage(String... message){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.reportMessage(message);
            } catch (RemoteException e){
                this.resetIOtoDefault();
                this.nativeReportMessage(message);
            }
        } else {
            this.nativeReportMessage(message);
        }
    }

    @Override
    public void reportError(String... error){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.reportError(error);
            } catch (RemoteException e){
                this.resetIOtoDefault();
                this.nativeReportError(false, error);
            }
        } else {
            this.nativeReportError(false, error);
        }    
    }
    
    @Override
    public void reportErrorAndExitLater(String... error){
        if (hasExternalIOProcessor){
            try{
                externalIOEngine.reportError(error);
                externalIOEngine.exitExternalIO();
            } catch (RemoteException e){
                this.resetIOtoDefault();
                this.nativeReportError(true, error);
            }
        } else {
            this.nativeReportError(true, error);
        }    
    }
    
    @Override
    public void reportException(Exception e, String... description){
        if (hasExternalIOProcessor){
            try {
                externalIOEngine.reportException(e, description);
            } catch (RemoteException re) {
                this.resetIOtoDefault();             
                this.nativeReportException(e, false, description);
            }
        } else {
            this.nativeReportException(e, false, description);
        }    
    }
    @Override
    public void reportExceptionAndExitLater(Exception e, String... description){
        if (hasExternalIOProcessor){
            try {
                externalIOEngine.reportException(e, description);
            } catch (RemoteException re) {
                this.resetIOtoDefault();             
                this.nativeReportException(e, true, description);
            }
        } else {
            this.nativeReportException(e, true, description);
        }    
    }
    
    @Override
    public int resolveVariantsWithExternalIO(String message, List<String> variants){
        int choosedVariant = 0;
        if (hasExternalIOProcessor){
            try {
                choosedVariant = externalIOEngine.chooseVariants(message, variants);
            } catch (RemoteException re) {
                this.resetIOtoDefault();
                choosedVariant = -2;
            }
        } else {
            choosedVariant = -2;
        }
        return choosedVariant;
    }
    
    /* ====================== Native Application output methods ===========================    
     * "Native" application methods for output. Are used when external output is 
     * unavailable or program should not use it`s own methods due to differnt circumstances
     * or behavior features.
     *
     * 'Native' output methods use JavaFX GUI to reportInfo user about events, errors etc.
     */
    private void nativeShowTask(Task task){                
        gui.showTask(task);        
    }

    private void nativeReportMessage(String[] info){
        gui.showMessage(info);
    }
    
    private void nativeReportError(boolean critical, String[] error){
        gui.showError(error);
        if (critical){
            gui.exitAfterAllWindowsClosed();
        }
    }
    
    private void nativeReportException(Exception e, boolean critical, String[] description){
        gui.showError(description);
        if (critical){
            gui.exitAfterAllWindowsClosed();
        }
    }
          
    private void resetIOtoDefault(){
        hasExternalIOProcessor = false;
        useExternalShowTaskMethod = false;
        externalIOEngine = null;
    }

    
    // ====================== RemoteControlModule method implementations =========================
    
    /*
     * Intended for accepting external object implements ExternalIOInterface with RMI
     * using given port and object`s name in RMI registry on given port.
     * Is invoked by ExternalIOInterface object to bind himself with organizer.
     */
    @Override
    public void acceptNewIOProcessor(ExternalIOInterface externalIo){
        this.externalIOEngine = externalIo;
        this.hasExternalIOProcessor = true;        
    }

    /*
     * Invoked by ExternalIOInterface object to get information about whether organizer 
     * already has a reference to external object implements ExternalIOInterface
     */
    @Override
    public boolean hasExternalIOProcessor(){
        try {            
            externalIOEngine.isActive();
            return true;
        } catch (RemoteException|NullPointerException e){
            this.resetIOtoDefault();
            return false;
        }          
    }
    
    @Override
    public void useExternalShowTaskMethod(){
        useExternalShowTaskMethod = true;
    }
    
    @Override
    public void useNativeShowTaskMethod(){
        useExternalShowTaskMethod = false;
    }     

    @Override
    public void setDefaultIO(){
        this.resetIOtoDefault();
    }
    
    @Override
    public void exitBeamServer(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                System.exit(0);
            }
        }).start();
    }
    
    @Override
    public void exitAfterAllNotifications(){
        this.gui.exitAfterAllWindowsClosed();
    }
}