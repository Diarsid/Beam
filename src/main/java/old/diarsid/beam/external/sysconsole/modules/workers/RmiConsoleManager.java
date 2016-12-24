/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.external.sysconsole.modules.workers;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import old.diarsid.beam.core.rmi.interfaces.RmiExecutorInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import old.diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

import old.diarsid.beam.external.ExternalIOInterface;

import old.diarsid.beam.external.sysconsole.exceptions.InterfacesLoadException;
import old.diarsid.beam.external.sysconsole.exceptions.RmiException;

import old.diarsid.beam.external.sysconsole.modules.ConsolePrinterModule;
import old.diarsid.beam.external.sysconsole.modules.RmiConsoleManagerModule;

import diarsid.beam.core.modules.ConfigModule;
import diarsid.beam.core.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class RmiConsoleManager implements RmiConsoleManagerModule {
    
    private final ConfigModule config;
    private final ConsolePrinterModule printer;
    
    private RmiTaskManagerInterface taskManager;
    private RmiExecutorInterface executor;
    private RmiRemoteControlInterface beamRemoteAccess;
    private RmiLocationsHandlerInterface locations;
    private RmiWebPagesHandlerInterface webPages;
    
    private Registry beamCoreRegistry;
    
    private boolean interfacesLoaded;
    
    RmiConsoleManager(ConfigModule config, ConsolePrinterModule pr) {
        this.config = config;
        this.printer = pr;
        this.interfacesLoaded = false;
    }
    
    @Override
    public void exportAndConnectToCore(ExternalIOInterface external) {
        
        try {
            // exporting external object
            int port = Integer.parseInt(config.get(Config.SYS_CONSOLE_PORT));
            Registry registry = LocateRegistry.createRegistry(port);
            ExternalIOInterface consoleStub =
                    (ExternalIOInterface) UnicastRemoteObject.exportObject(external, port);
            registry.bind(config.get(Config.SYS_CONSOLE_NAME), consoleStub);
            
            // connecting with Beam remote control
            if (this.beamRemoteAccess.isExternalIoProcessorActive()){
                showProblemMessageAndClose("Organizer already has external output!");
            } else {
                this.beamRemoteAccess.acceptNewIOProcessor(config.get(Config.SYS_CONSOLE_NAME), 
                        config.get(Config.SYS_CONSOLE_HOST), 
                        Integer.parseInt(config.get(Config.SYS_CONSOLE_PORT)));
                this.beamRemoteAccess.setUseNativeShowTaskMethod();                
            }
            
        } catch(ExportException e) {
            showProblemMessageAndClose("Console export: this port already in use.");
        } catch (AlreadyBoundException abe) {
            showProblemMessageAndClose("Console export failure: AlreadyBoundException");
        } catch (NotBoundException e) {
            showProblemMessageAndClose("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re) {
            showProblemMessageAndClose("Console export failure: RemoteException");
        }
    }
    
    @Override
    public void loadBeamCoreInterfaces() {
        
        try {
            this.beamCoreRegistry = LocateRegistry
                    .getRegistry(config.get(Config.CORE_HOST), 
                            Integer.parseInt(config.get(Config.CORE_PORT))); 
            
            this.beamRemoteAccess = (RmiRemoteControlInterface) 
                    this.beamCoreRegistry.lookup(config.get(Config.CORE_ACCESS_ENDPOINT));
            
            this.taskManager = (RmiTaskManagerInterface) 
                    this.beamCoreRegistry.lookup(config.get(Config.TASK_MANAGER_NAME));
            
            this.executor = (RmiExecutorInterface) 
                    this.beamCoreRegistry.lookup(config.get(Config.EXECUTOR_NAME));
            
            this.locations = (RmiLocationsHandlerInterface) 
                    this.beamCoreRegistry.lookup(config.get(Config.LOCATIONS_HANDLER_NAME));
            
            this.webPages = (RmiWebPagesHandlerInterface) 
                    this.beamCoreRegistry.lookup(config.get(Config.WEB_PAGES_HANDLER_NAME));
            
            this.interfacesLoaded = true;
            
        } catch (NotBoundException e) { 
            e.printStackTrace();
            showProblemMessageAndClose("Connecting to Organizer failure: NotBoundException");
        } catch (RemoteException re) {
            re.printStackTrace();
            showProblemMessageAndClose("Connecting to Organizer failure: RemoteException");
        }
    }
 
    private void showProblemMessageAndClose(String message){
        try {
            this.printer.printBeamErrorWithMessageLn(new String[] {message});
            throw new RmiException();
        } catch(IOException e){}
    }
    
    @Override
    public RmiExecutorInterface getExecutor() {
        if ( this.interfacesLoaded ) {
            return this.executor;
        } 
        throw new InterfacesLoadException(
                "Beam.core remote intefaces have not been loaded -> " + 
                "RmiConsoleManager::loadBeamCoreInterfaces() " +
                "had not been invoked.");
    }
    
    @Override
    public RmiLocationsHandlerInterface getLocationsHandler() {
        if ( this.interfacesLoaded ) {
            return this.locations;
        } 
        throw new InterfacesLoadException(
                "Beam.core remote intefaces have not been loaded -> " + 
                "RmiConsoleManager::loadBeamCoreInterfaces() " +
                "had not been invoked.");
    }
    
    @Override
    public RmiRemoteControlInterface getRemoteControl() {
        if ( this.interfacesLoaded ) {
            return this.beamRemoteAccess;
        } 
        throw new InterfacesLoadException(
                "Beam.core remote intefaces have not been loaded -> " + 
                "RmiConsoleManager::loadBeamCoreInterfaces() " +
                "had not been invoked.");
    }
    
    @Override
    public RmiTaskManagerInterface getTaskManager() {
        if ( this.interfacesLoaded ) {
            return this.taskManager;
        } 
        throw new InterfacesLoadException(
                "Beam.core remote intefaces have not been loaded -> " + 
                "RmiConsoleManager::loadBeamCoreInterfaces() " +
                "had not been invoked.");
    }
    
    @Override
    public RmiWebPagesHandlerInterface getWebPageHandler() {
        if ( this.interfacesLoaded ) {
            return this.webPages;
        } 
        throw new InterfacesLoadException(
                "Beam.core remote intefaces have not been loaded -> " + 
                "RmiConsoleManager::loadBeamCoreInterfaces() " +
                "had not been invoked.");
    }        
}
