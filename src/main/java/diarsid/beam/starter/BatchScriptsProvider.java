/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.starter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import diarsid.beam.core.Beam;
import diarsid.beam.external.sysconsole.SysConsole;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class BatchScriptsProvider {
    
    private final ConfigModule config;
    private final boolean track;
    private final BatchSrciptsComposer scriptsComposer;
    
    private final String beamCoreRunScriptFile = "./Beam.core_run";
    private final String beamSysConsoleRunScriptFile = "./Beam.sysconsole_run";    
    private final String beamShellLoaderScript = "./Beam.shell-loader_run";
    private final String beamBatchLoaderScript = "./Beam.loader_run";
    private final String beamSystemPathScript = "./beam";
    
    private Path coreScript;
    private Path sysConsoleScript;
    private Path shellLoaderScript;
    private Path batchLoaderScript;
    private Path systemPathScript;
    
    BatchScriptsProvider(ConfigModule config, boolean track) {
        this.config = config;
        this.track = track;
        this.scriptsComposer = new BatchSrciptsComposer();
    }
    
    void processScripts() {        
        String ext = this.getSystemScriptsExtension();
        
        this.coreScript = Paths.get(this.beamCoreRunScriptFile + ext);
        this.sysConsoleScript = Paths.get(this.beamSysConsoleRunScriptFile + ext);
        this.shellLoaderScript = Paths.get(this.beamShellLoaderScript + ext);
        this.batchLoaderScript = Paths.get(this.beamBatchLoaderScript + ext);       
        this.systemPathScript = Paths.get(this.beamSystemPathScript + ext);
        
        try {
            this.writeBatchLoaderScript();
            this.writeSystemPathLoaderScript();
            if ( !Files.exists(this.coreScript) ) {
                this.track(" > core script does not exist.");
                this.writeCoreScript();                
            }
            if ( !Files.exists(this.sysConsoleScript) ) {
                this.track(" > separate system console script does not exist."); 
                this.writeSeparateSysConsoleScript();                
            }
            if ( !Files.exists(this.shellLoaderScript)) {
                this.track(" > shell loader script does not exist.");
                this.writeShellLoaderScript();
            }
            if ( !this.coreScriptNotActual() ) {
                this.track(" > core script is not up to date."); 
                this.writeCoreScript();
            }
            if ( !this.sysConsoleScriptNotActual() ) {
                this.track(" > system console script is not up to date.");
                this.writeSeparateSysConsoleScript();
            }  
            if ( !this.shellLoaderScriptNotActual() ) {
                this.track(" > shell loader script is not up to date.");
                this.writeShellLoaderScript();
            }
            track("All scripts are up to date.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void track(String trackInfo) {
        if (track) {
            System.out.println(trackInfo);
        }
    }
    
    private void createScriptFile(Path script, List<String> lines) 
            throws IOException {
        
        Files.write(script, lines, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }
    
    private void writeSystemPathLoaderScript() throws IOException {
        String pathToBinaries = this.systemPathScript.getParent().toAbsolutePath().normalize().toString();
        String pathToScript = this.shellLoaderScript.toAbsolutePath().normalize().toString();
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.scriptsComposer.composeSystemPathCMDScript(
                    pathToBinaries, 
                    pathToScript);
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.scriptsComposer.composeSystemPathShellScript(
                    pathToBinaries, 
                    pathToScript);
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        this.createScriptFile(this.systemPathScript, scriptLines);        
        
        if (track) { System.out.println(" > system path script created."); }
    }
    
    private void writeBatchLoaderScript() throws IOException {
        String loaderClasspath = ".;.\\..\\lib\\gem-injector-1.0.jar;Beam.jar";
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.scriptsComposer.composeBatchLoaderCMDScript(
                    loaderClasspath, 
                    BeamPartsBatchLoader.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.scriptsComposer.composeBatchLoaderShellScript(
                    loaderClasspath, 
                    BeamPartsBatchLoader.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        this.createScriptFile(this.batchLoaderScript, scriptLines);        
        
        if (track) { System.out.println(" > batch loader script created."); }
    }
    
    private void writeCoreScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_CORE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_CORE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.scriptsComposer.composeCoreCMDScript(
                    configHashCode, 
                    coreClasspath, 
                    jvmOptions, 
                    Beam.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.scriptsComposer.composeCoreShellScript(
                    configHashCode, 
                    coreClasspath, 
                    jvmOptions, 
                    Beam.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        this.createScriptFile(this.coreScript, scriptLines);       
        
        if (track) { System.out.println(" > core script created."); }
    }  
            
    private void writeSeparateSysConsoleScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String consoleClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + consoleClasspath.hashCode();
        
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.scriptsComposer.composeSysConsoleCMDScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    SysConsole.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.scriptsComposer.composeSysConsoleShellScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    SysConsole.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        this.createScriptFile(this.sysConsoleScript, scriptLines);        
        
        if (track) { System.out.println(" > separate system console script created."); }
    }
    
    private void writeShellLoaderScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String consoleClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + consoleClasspath.hashCode();
        
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.scriptsComposer.composeShellLoaderCMDScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    BeamPartsShellLoader.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.scriptsComposer.composeShellLoaderShellScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    BeamPartsShellLoader.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        this.createScriptFile(this.shellLoaderScript, scriptLines);        
        
        if (track) { System.out.println(" > shell loader script created."); }
    }
    
    private boolean coreScriptNotActual() throws IOException {
        String jvmOptions = config.get(Config.JVM_CORE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_CORE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        return this.isThisScriptActual(this.coreScript, configHashCode);
    }
    
    private boolean sysConsoleScriptNotActual() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        return this.isThisScriptActual(this.sysConsoleScript, configHashCode);
    }
    
    private boolean shellLoaderScriptNotActual() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        return this.isThisScriptActual(this.shellLoaderScript, configHashCode);        
    }
    
    private boolean isThisScriptActual(Path script, int configHashCode) 
            throws IOException {
        
        String firstLine = Files.readAllLines(script).get(0);
        
        int scriptHashCode = Integer.parseInt(firstLine.substring(
                firstLine.indexOf("[")+1,
                firstLine.indexOf("]")));
        
        return (configHashCode == scriptHashCode);
    }
    
    private String getSystemScriptsExtension() {
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            return ".bat";
        } else if (systemName.contains("x") || systemName.contains("u")){
            return ".sh";
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return null;
        }
    }
    
    File getBeamCoreScript() throws IOException {
        return this.coreScript.toFile();
    }
    
    File getBeamSysConsoleScript() throws IOException {
        return this.sysConsoleScript.toFile();
    }
}
