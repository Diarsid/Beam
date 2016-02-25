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
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.Beam;

import diarsid.beam.core.exceptions.TemporaryCodeException;

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
    
    private final String beamCoreRunScriptFile = "./Beam.core_run";
    private final String beamSysConsoleRunScriptFile = "./Beam.sysconsole_run";    
    
    private Path coreScript;
    private Path sysConsoleScript;
    
    BatchScriptsProvider(ConfigModule config, boolean track) {
        this.config = config;
        this.track = track;
    }
    
    void processScripts() {        
        String ext = getSystemScriptsExtension();
        this.coreScript = Paths.get(this.beamCoreRunScriptFile + ext);
        this.sysConsoleScript = Paths.get(this.beamSysConsoleRunScriptFile + ext);
        
        try {
            if ( !Files.exists(this.coreScript) ) {
                this.track(" > core script does not exist.");
                this.writeCoreScript();                
            }
            if ( !Files.exists(this.sysConsoleScript) ) {
                this.track(" > system console script does not exist."); 
                this.writeSysConsoleScript();                
            }
            if ( !this.coreScriptNotActual() ) {
                this.track(" > core script is not up to date."); 
                this.writeCoreScript();
            }
            if ( !this.sysConsoleScriptNotActual() ) {
                this.track(" > system console script is not up to date.");
                this.writeSysConsoleScript();
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
    
    private void writeCoreScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_CORE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_CORE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.writeCoreCMDScript(
                    configHashCode, 
                    coreClasspath, 
                    jvmOptions, 
                    Beam.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.writeCoreShellScript(
                    configHashCode, 
                    coreClasspath, 
                    jvmOptions, 
                    Beam.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        Files.write(this.coreScript, 
                scriptLines, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        
        if (track) { System.out.println(" > core script created."); }
    }
    
    private List<String> writeCoreCMDScript(
            int configHashCode, 
            String coreClassPath, 
            String jvmOptions, 
            String className) {
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(":: configuration hashcode [" + configHashCode + "]");
        scriptLines.add("");
        scriptLines.add("@echo off");
        scriptLines.add(
                "cmd /c start javaw -cp .;" + 
                coreClassPath + 
                " -Djava.security.policy=.\\..\\config\\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + className);
        
        return scriptLines;
    }
    
    private List<String> writeSysConsoleCMDScript(
            int configHashCode, 
            String consoleClasspath, 
            String jvmOptions, 
            String className) {
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(":: configuration hashcode [" + configHashCode + "]");
        scriptLines.add("");
        scriptLines.add("@echo off");
        scriptLines.add(
                "cmd /c start java -cp .;" + 
                consoleClasspath + 
                " -Djava.security.policy=.\\..\\config\\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + className);
        
        return scriptLines;
    }
    
    private List<String> writeCoreShellScript(
            int configHashCode, 
            String coreClasspath, 
            String jvmOptions, 
            String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
    
    private List<String> writeSysConsoleShellScript(
            int configHashCode, 
            String consoleClassPath, 
            String jvmOptions, 
            String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
            
    private void writeSysConsoleScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String consoleClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + consoleClasspath.hashCode();
        
        List<String> scriptLines;
        
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            scriptLines = this.writeSysConsoleCMDScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    SysConsole.class.getCanonicalName());
        } else if (systemName.contains("x") || systemName.contains("u")){
            scriptLines = this.writeSysConsoleShellScript(
                    configHashCode, 
                    consoleClasspath, 
                    jvmOptions, 
                    SysConsole.class.getCanonicalName());
        } else {
            System.out.println("Unknown OS.");
            System.exit(1);
            return;
        }
        
        Files.write(this.sysConsoleScript, 
                scriptLines, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        
        if (track) { System.out.println(" > system console script created."); }
    }
    
    private boolean coreScriptNotActual() throws IOException {
        String jvmOptions = config.get(Config.JVM_CORE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_CORE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        String firstLine = Files.readAllLines(this.coreScript).get(0);
        
        int scriptHashCode = Integer.parseInt(firstLine.substring(
                firstLine.indexOf("[")+1,
                firstLine.indexOf("]")));
        
        return (configHashCode == scriptHashCode);
    }
    
    private boolean sysConsoleScriptNotActual() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        String firstLine = Files.readAllLines(this.sysConsoleScript).get(0);
        
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
