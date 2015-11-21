/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.innerio;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.exceptions.NullDependencyInjectionException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.IoModule;
import com.drs.beam.core.modules.tasks.Task;

/**
 *
 * @author Diarsid
 */
class IoInnerModuleWorker implements IoInnerModule {
    
    private final IoModule io;
    private final Gui gui;

    IoInnerModuleWorker(IoModule ioModule, Gui gui) {
        if (ioModule == null) {
            throw new NullDependencyInjectionException(
                    IoInnerModuleWorker.class.getSimpleName(), 
                    IoModule.class.getSimpleName());
        }
        if (gui == null){
            throw new NullDependencyInjectionException(
                    IoInnerModuleWorker.class.getSimpleName(), 
                    Gui.class.getSimpleName());
        }
        this.io = ioModule;
        this.gui = gui;
    }
    
    @Override
    public void showTask(Task task){
        if (this.io.hasExternalIOProcessor() && io.useExternalShowTaskMethod()){
            try{
                this.io.getExternalIOEngine().showTask(task);
            } catch (RemoteException e){
                this.io.resetIoToDefault();
                this.nativeShowTask(task);
            }
        } else {
            this.nativeShowTask(task);
        }
    }
    
    @Override
    public void reportInfo(String... info){
        if (this.io.hasExternalIOProcessor()){
            try{
                this.io.getExternalIOEngine().reportInfo(info);
            } catch (RemoteException e){
                this.io.resetIoToDefault();
                this.nativeReportMessage(info);
            }
        } else {
            this.nativeReportMessage(info);
        }    
    }
    
    @Override
    public void reportMessage(String... message){
        if (this.io.hasExternalIOProcessor()){
            try{
                this.io.getExternalIOEngine().reportMessage(message);
            } catch (RemoteException e){
                this.io.resetIoToDefault();
                this.nativeReportMessage(message);
            }
        } else {
            this.nativeReportMessage(message);
        }
    }

    @Override
    public void reportError(String... error){
        if (this.io.hasExternalIOProcessor()){
            try{
                this.io.getExternalIOEngine().reportError(error);
            } catch (RemoteException e){
                this.io.resetIoToDefault();
                this.nativeReportError(false, error);
            }
        } else {
            this.nativeReportError(false, error);
        }    
    }
    
    @Override
    public void reportErrorAndExitLater(String... error){
        if (this.io.hasExternalIOProcessor()){
            try{
                this.io.getExternalIOEngine().reportError(error);
                this.io.getExternalIOEngine().exitExternalIO();
            } catch (RemoteException e){
                this.io.resetIoToDefault();
                this.nativeReportError(true, error);
            }
        } else {
            this.nativeReportError(true, error);
        }    
    }
    
    @Override
    public void reportException(Exception e, String... description){
        if (this.io.hasExternalIOProcessor()){
            try {
                this.io.getExternalIOEngine().reportException(description);
            } catch (RemoteException re) {
                this.io.resetIoToDefault();           
                this.nativeReportException(e, false, description);
            }
        } else {
            this.nativeReportException(e, false, description);
        }    
    }
    @Override
    public void reportExceptionAndExitLater(Exception e, String... description){
        if (this.io.hasExternalIOProcessor()){
            try {
                this.io.getExternalIOEngine().reportException(description);
            } catch (RemoteException re) {
                this.io.resetIoToDefault();            
                this.nativeReportException(e, true, description);
            }
        } else {
            this.nativeReportException(e, true, description);
        }    
    }
    
    @Override
    public int resolveVariantsWithExternalIO(String message, List<String> variants){
        int choosedVariant = 0;
        if (this.io.hasExternalIOProcessor()){
            try {
                choosedVariant = this.io.getExternalIOEngine()
                        .chooseVariants(message, variants);
            } catch (RemoteException re) {
                this.io.resetIoToDefault();
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
}
