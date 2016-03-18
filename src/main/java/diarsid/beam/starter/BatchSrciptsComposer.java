/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.starter;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.exceptions.TemporaryCodeException;

/**
 *
 * @author Diarsid
 */
class BatchSrciptsComposer {
    
    BatchSrciptsComposer() {
    }    
    
    List<String> composeCoreCMDScript(
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
                " -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + className);
        
        return scriptLines;
    }
    
    List<String> composeSysConsoleCMDScript(
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
                " -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + className);
        
        return scriptLines;
    }
    
    List<String> composeShellLoaderCMDScript(
            int configHashCode, 
            String consoleClasspath, 
            String jvmOptions, 
            String className) {
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add(":: configuration hashcode [" + configHashCode + "]");
        scriptLines.add("");
        scriptLines.add("@echo off");
        scriptLines.add(
                "java -cp .;" + 
                consoleClasspath + 
                " -Djava.rmi.server.hostname=127.0.0.1 " + 
                jvmOptions + " " + className);
        
        return scriptLines;
    }
    
    List<String> composeBatchLoaderCMDScript(
            String loaderClasspath, String className) {
        
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add("@echo off");
        scriptLines.add(
                "cmd /c start javaw -cp " + 
                loaderClasspath +
                " " + className);
        
        return scriptLines;
    }
    
    List<String> composeSystemPathCMDScript(String pathBinaries, String pathScript) {
        List<String> scriptLines = new ArrayList<>();
        scriptLines.add("@echo off");
        scriptLines.add("echo loading...");
        scriptLines.add("echo.");
        scriptLines.add("@echo off");
        scriptLines.add("set OLDDIR=%cd%");
        scriptLines.add("cd /D " + pathBinaries.replace("/", "\\"));
        scriptLines.add("call " + pathScript.replace("/", "\\"));
        scriptLines.add("cd /D %OLDDIR%");
        return scriptLines;
    }
     
    List<String> composeCoreShellScript(
            int configHashCode, 
            String coreClasspath, 
            String jvmOptions, 
            String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
    
    List<String> composeSysConsoleShellScript(
            int configHashCode, 
            String consoleClassPath, 
            String jvmOptions, 
            String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
    
    List<String> composeShellLoaderShellScript(
            int configHashCode, 
            String consoleClassPath, 
            String jvmOptions, 
            String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
    
    List<String> composeBatchLoaderShellScript(
            String loaderClasspath, String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
    
    List<String> composeSystemPathShellScript(
            String loaderClasspath, String className) {
        
        // not implemented yet.
        throw new TemporaryCodeException("Shell script creation not implemented.");
    }
}
