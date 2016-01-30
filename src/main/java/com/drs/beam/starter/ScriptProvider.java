/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.starter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.drs.beam.core.Beam;
import com.drs.beam.external.sysconsole.SysConsole;
import com.drs.beam.shared.modules.ConfigModule;
import com.drs.beam.shared.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class ScriptProvider {
    
    private final ConfigModule config;
    
    private final String beamCoreRunScriptFile = "./Beam.core_run";
    private final String beamSysConsoleRunScriptFile = "./Beam.sysconsole_run";    
    
    private Path coreScript;
    private Path sysConsoleScript;
    
    ScriptProvider(ConfigModule config) {
        this.config = config;
    }
    
    void processScripts() {
        String ext = getSystemScriptsExtension();
        this.coreScript = Paths.get(this.beamCoreRunScriptFile + ext);
        this.sysConsoleScript = Paths.get(this.beamSysConsoleRunScriptFile + ext);
        
        try {
            if ( !Files.exists(this.coreScript) ) {
                this.writeCoreScript();
            }
            if ( !Files.exists(this.sysConsoleScript) ) {
                this.writeSysConsoleScript();
            }
            if ( !this.coreScriptNotActual() ) {
                this.writeCoreScript();
            }
            if ( !this.sysConsoleScriptNotActual() ) {
                this.writeSysConsoleScript();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeCoreScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_CORE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_CORE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(":: configuration hashcode [" + configHashCode + "]");
        scriptLines.add("");
        scriptLines.add("@echo off");
        scriptLines.add(
                "cmd /c start javaw -cp .;" + 
                coreClasspath + 
                " -Djava.security.policy=.\\..\\config\\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + Beam.class.getCanonicalName());
        
        Files.write(this.coreScript, 
                scriptLines, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }
    
    private void writeCoreCMDScript(String con) {
        
    }
            
    private void writeSysConsoleScript() throws IOException {
        String jvmOptions = config.get(Config.JVM_SYS_CONSOLE_OPTIONS);
        String coreClasspath = config.get(Config.CLASSPATH_SYS_CONSOLE).replace(" ", ";");
        
        int configHashCode = jvmOptions.hashCode() + coreClasspath.hashCode();
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(":: configuration hashcode [" + configHashCode + "]");
        scriptLines.add("");
        scriptLines.add("@echo off");
        scriptLines.add(
                "cmd /c start java -cp .;" + 
                coreClasspath + 
                " -Djava.security.policy=.\\..\\config\\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + SysConsole.class.getCanonicalName());
        
        Files.write(this.sysConsoleScript, 
                scriptLines, 
                StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
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
