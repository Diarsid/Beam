/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.starter;

import diarsid.beam.core.Beam;
import diarsid.beam.core.application.ScriptsCatalog;
import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.starter.FlagConfigurable;
import diarsid.beam.core.starter.FlagExecutable;
import diarsid.beam.core.starter.FlagStartable;
import diarsid.beam.core.starter.Procedure;

import static diarsid.beam.core.util.Logs.disableConsoleDebugging;
import static diarsid.beam.core.util.Logs.disableFileDebugging;
import static diarsid.beam.core.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class StartRunner {
    
    private final Configuration configuration;
    private final ScriptsCatalog scriptsCatalog;
    
    public StartRunner(Configuration configuration, ScriptsCatalog scriptsCatalog) {
        this.configuration = configuration;
        this.scriptsCatalog = scriptsCatalog;
    }
    
    void process(Procedure procedure) {        
        if ( procedure.hasAnyExecutables() ) {
            procedure.getExecutables().forEach(this::processExecutable);
        }
        if ( procedure.hasAnyConfigurables() ) {
            procedure.getConfigurables().forEach(this::processConfigurable);
        }
        if ( procedure.hasStartable() ) {
            this.processStartable(procedure.getStartable());
        }
    }
    
    private void processStartable(FlagStartable startable) {
        switch ( startable ) {
            case START_ALL : {
                log(this.getClass(), "launching all components as separate processes...");
                
                break;
            }    
            case START_CORE : {
                
                break;
            }    
            case START_CORE_INLINE : {
                log(this.getClass(), "launching Beam.core in inline mode...");
                Beam.main();
                break;
            }    
            case START_SYSTEM_CONSOLE : {
                
                break;
            }    
            case START_SYSTEM_CONSOLE_INLINE : {
                
                break;
            }    
            default : {}
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
    
    private void processExecutable(FlagExecutable executable) {
        switch ( executable ) {
            case REWRITE_SCRIPTS : {
                this.scriptsCatalog.rewriteScripts();
                break;
            }    
            default : {}         
        }
    }
}
