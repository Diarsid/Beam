/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.starter;

import java.io.IOException;

import old.diarsid.beam.external.sysconsole.SysConsole;

import diarsid.beam.core.modules.config.ConfigHolderModuleWorkerBuilder;
import diarsid.beam.core.modules.ConfigHolderModule;

/**
 *
 * @author Diarsid
 */
public class BeamPartsBatchLoader {
    
    static String SHELL_MODE_OPTION = "-shell";
    private static boolean shellMode = false;
    private static BeamPartsRemoteLocator locator;
    private static BatchScriptsExecutor starter;
    
    private BeamPartsBatchLoader() {        
    }
    
    public static void main(String... args) throws IOException {
        ifStartInShellMode(args);
        prepare();
        if ( locator.shouldStartAnything() ) {
            startAllNecessaryModules();
        }        
    }
    
    private static void ifStartInShellMode(String[] args) {
        for (String arg : args) {
            if ( SHELL_MODE_OPTION.equals(arg) ) {
                shellMode = true;
                return;
            }
        }
        shellMode = false;
    }
    
    private static void prepare() throws IOException {
        ConfigHolderModule config = provideConfig();
        locator = new BeamPartsRemoteLocator(config);
        BatchScriptsProvider scripts = new BatchScriptsProvider(config, false);
        scripts.processScripts();
        starter = new BatchScriptsExecutor(scripts);
    }
    
    private static ConfigHolderModule provideConfig() {
        ConfigHolderModuleWorkerBuilder confBuilder = new ConfigHolderModuleWorkerBuilder();
        ConfigHolderModule config = confBuilder.buildModule();
        return config;
    }

    private static void startAllNecessaryModules() 
                    throws IOException {
        
        if ( locator.shouldStartBeamCore() ) {
            starter.runBeam();
            waitAndStartSysConsole();
        } else if ( locator.shouldStartBeamSysConsole() ) {
            startSysConsoleAccordingToMode();
        }
    }

    private static void startSysConsoleAccordingToMode() throws IOException {
        if ( shellMode ) {
            SysConsole.main();
        } else {
            starter.runConsole();
        }
    }

    private static void waitAndStartSysConsole() 
                throws IOException {
        
        if ( locator.shouldStartBeamSysConsole() ) {
            while ( ! locator.isBeamCoreWorkingNow() ) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    System.out.println(ie.getMessage());
                }
            }
            startSysConsoleAccordingToMode();
        }
    }
}
