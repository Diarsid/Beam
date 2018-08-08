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
import static diarsid.beam.core.base.util.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class Launcher {
    
    private final static Launcher LAUNCHER;
    
    static {
        LAUNCHER = new Launcher(
                configuration(), 
                scriptsCatalog(), 
                librariesCatalog());
    }
    
    private final Configuration config;
    private final ScriptsCatalog scriptsCatalog;
    private final LibrariesCatalog librariesCatalog;
    
    private Launcher(
            Configuration configuration, 
            ScriptsCatalog scriptsCatalog, 
            LibrariesCatalog librariesCatalog) {
        this.config = configuration;
        this.scriptsCatalog = scriptsCatalog;
        this.librariesCatalog = librariesCatalog;
    }
    
    public static Launcher getLauncher() {
        return LAUNCHER;
    }
    
    void launch(Procedure procedure) {
        if ( procedure.hasLaunchable() ) {
            this.processStartable(procedure.getLaunchable());
        }
    }
    
    private void processStartable(FlagLaunchable startable) {
        switch ( startable ) {
            case START_ALL : {
                logFor(this).info("launching core...");
                this.executeCoreScript();
                logFor(this).info("launching sysconsole...");
                this.awaitCore();
                this.executeSysConsoleScript();
                break;
            }    
            case START_CORE : {
                logFor(this).info("launching core...");
                this.executeCoreScript();
                break;
            }  
            case START_SYSTEM_CONSOLE : {
                logFor(this).info("launching sysconsole...");
                this.awaitCore();                
                this.executeSysConsoleScript();
                break;
            }  
            default : {
                logFor(this).info("unknown startable component: " + startable.text());
            }
        }
    }

    public void executeCoreScript() throws RequirementException {
        Optional<Script> coreScript = this.scriptsCatalog.findScriptByName("beam.core");
        if ( coreScript.isPresent() ) {
            coreScript.get().execute();
        } else {
            this.scriptsCatalog
                    .newScript("beam.core")
                    .invokeClass(Beam.class)
                    .usingJavaw()
                    .withClasspath(this.librariesCatalog.libraries())
                    .withJvmOptions(this.config.asList("core.jvm.option"))
                    .complete()
                    .save()
                    .execute();
        }
    }
    
    public void executeSysConsoleScript() throws RequirementException {
        Optional<Script> sysconsoleScript = this.scriptsCatalog.findScriptByName("beam.sysconsole");
        if ( sysconsoleScript.isPresent() ) {
            sysconsoleScript.get().execute();
        } else {
            this.scriptsCatalog
                    .newScript("beam.sysconsole")
                    .invokeClass(SystemConsole.class)
                    .withClasspath(this.librariesCatalog.librariesWithAny("log", "slf"))
                    .withJvmOptions(this.config.asList("sysconsole.jvm.option"))
                    .complete()
                    .save()
                    .execute();
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
            logFor(this).error(e.getMessage(), e);
            throw new WorkflowBrokenException("waiting for core has been interrupted.");
        }
    }
}
