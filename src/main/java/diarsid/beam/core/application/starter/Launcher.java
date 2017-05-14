/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Optional;

import diarsid.beam.core.Beam;
import diarsid.beam.core.application.environment.Configuration;
import diarsid.beam.core.application.environment.LibrariesCatalog;
import diarsid.beam.core.application.environment.Script;
import diarsid.beam.core.application.environment.ScriptsCatalog;
import diarsid.beam.core.application.systemconsole.SystemConsole;
import diarsid.beam.core.base.exceptions.RequirementException;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.rmi.registry.LocateRegistry.getRegistry;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.application.environment.BeamEnvironment.librariesCatalog;
import static diarsid.beam.core.application.environment.BeamEnvironment.scriptsCatalog;
import static diarsid.beam.core.base.rmi.RmiComponentNames.CORE_ACCESS_ENDPOINT_NAME;
import static diarsid.beam.core.base.util.Logs.disableConsoleDebugging;
import static diarsid.beam.core.base.util.Logs.disableFileDebugging;
import static diarsid.beam.core.base.util.Logs.log;
import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
class Launcher {
    
    private final Configuration config;
    private final ScriptsCatalog scriptsCatalog;
    private final LibrariesCatalog librariesCatalog;
    
    Launcher(
            Configuration configuration, 
            ScriptsCatalog scriptsCatalog, 
            LibrariesCatalog librariesCatalog) {
        this.config = configuration;
        this.scriptsCatalog = scriptsCatalog;
        this.librariesCatalog = librariesCatalog;
    }
    
    static Launcher getLauncher() {
        return new Launcher(
                configuration(), 
                scriptsCatalog(), 
                librariesCatalog());
    }
    
    void launch(Procedure procedure) {
        if ( procedure.hasAnyConfigurables() ) {
            procedure.getConfigurables().forEach(this::processConfigurable);
        }
        if ( procedure.hasLaunchable() ) {
            this.processStartable(procedure.getLaunchable());
        }
    }
    
    private void processStartable(FlagLaunchable startable) {
        switch ( startable ) {
            case START_ALL : {
                log(this.getClass(), "launching core...");
                this.executeCoreScript();
                log(this.getClass(), "launching sysconsole...");
                this.awaitCore();
                this.executeSysConsoleScript();
                break;
            }    
            case START_CORE : {
                log(this.getClass(), "launching core...");
                this.executeCoreScript();
                break;
            }  
            case START_SYSTEM_CONSOLE : {
                log(this.getClass(), "launching sysconsole...");
                this.awaitCore();                
                this.executeSysConsoleScript();
                break;
            }  
            default : {
                log(this.getClass(), "unknown startable component: " + startable.text());
            }
        }
    }

    private void executeCoreScript() throws RequirementException {
        Optional<Script> coreScript = this.scriptsCatalog.getScriptByName("beam.core");
        if ( coreScript.isPresent() ) {
            coreScript.get().execute();
        } else {
            this.scriptsCatalog
                    .newScript("beam.core")
                    .invokeClass(Beam.class)
                    .usingJavaw()
                    .withClasspath(this.librariesCatalog.getLibraries())
                    .withJvmOptions(this.config.getAsList("core.jvm.option"))
                    .complete()
                    .save()
                    .execute();
        }
    }
    
    private void executeSysConsoleScript() throws RequirementException {
        Optional<Script> coreScript = this.scriptsCatalog.getScriptByName("beam.sysconsole");
        if ( coreScript.isPresent() ) {
            coreScript.get().execute();
        } else {
            this.scriptsCatalog
                    .newScript("beam.sysconsole")
                    .invokeClass(SystemConsole.class)
                    .withClasspath(this.librariesCatalog.getLibrariesWithAny("log", "slf"))
                    .withJvmOptions(this.config.getAsList("sysconsole.jvm.option"))
                    .complete()
                    .save()
                    .execute();
        }
    }
    
    private void processConfigurable(FlagConfigurable configurable) {
        switch ( configurable ) {
            case NO_DEBUG : {
                disableConsoleDebugging();
                log(this.getClass(), "console debugging disabled!");
                disableFileDebugging();
                log(this.getClass(), "file debugging disabled!");
                break;
            }
            case NO_FILE_DEBUG : {
                log(this.getClass(), "file debugging disabled!");
                disableFileDebugging();
                break;
            }
            case NO_CONSOLE_DEBUG : {
                log(this.getClass(), "console debugging disabled!");
                disableConsoleDebugging();
                break;
            }
            case NO_CONSOLE_LOG : {
                break;
            }    
            default : {}            
        }
    }
    
    private void awaitCore() {
        int coreRegistryPort = parseInt(this.config.asString("rmi.core.port"));
        String coreRegistryHost = this.config.asString("rmi.core.host");
        int awaitCounter = 0;
        int sleep = 100;
        boolean coreIsNotReady = true;
        Registry registry;
        RemoteCoreAccessEndpoint remoteAccess;
        try {
            while ( coreIsNotReady && ( awaitCounter < 40 ) ) {                
                sleep(sleep);
                awaitCounter++;
                try {
                    registry = getRegistry(coreRegistryHost, coreRegistryPort);
                    remoteAccess = (RemoteCoreAccessEndpoint) 
                            registry.lookup(CORE_ACCESS_ENDPOINT_NAME);
                    return;
                } catch (RemoteException|NotBoundException ex) {
                    // do nothing, wait for core to be ready for connection.
                } 
            }        
            throw new WorkflowBrokenException(
                    format("cannot connect to core for %d millis", (sleep * awaitCounter)));
        } catch (InterruptedException e) {
            logError(Launcher.class, e);
            throw new WorkflowBrokenException("waiting for core has been interrupted.");
        }
    }
}
