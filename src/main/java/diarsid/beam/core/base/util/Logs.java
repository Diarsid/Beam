/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Diarsid
 */
public class Logs {
    
    private static Logger fileDebugger; 
    private static Logger consoleDebugger; 
    private static boolean useFileDebugging;
    private static boolean useConsoleDebugging;
    private static boolean useConsoleLogging;
    
    static {
        useConsoleLogging = true;
        useConsoleDebugging = true;
        useFileDebugging = true;
        fileDebugger = LoggerFactory.getLogger("fileDebugger");
        consoleDebugger = LoggerFactory.getLogger("consoleDebugger");
    }
    
    private Logs() {
    }
    
    public static void disableFileDebugging() {
        useFileDebugging = false;
    }
    
    public static void disableConsoleDebugging() {
        useConsoleDebugging = false;
    }
    
    public static void debug(String log) {
        if ( useConsoleDebugging ) {
            consoleDebugger.debug(log);
        }
//        if ( useFileDebugging ) {
//            fileDebugger.debug(log); 
//        }
    }
    
    public static void log(Class clazz, String log) {
        LoggerFactory.getLogger(clazz).info(log);
    }
    
    public static void warn(Class clazz, String warning) {
        LoggerFactory.getLogger(clazz).warn(warning);
    }
    
    public static void logError(Class clazz, String log, String... description) {
        Logger l = LoggerFactory.getLogger(clazz); 
        l.error(log);
        for (String s : description) {
            l.error(s);
        }
    }
    
    public static void logError(Class clazz, String log) {
        LoggerFactory.getLogger(clazz).error(log); 
    }
    
    public static void logError(Class clazz, Throwable e) {
        LoggerFactory.getLogger(clazz).error("", e); 
    }
    
    public static void logError(Class clazz, String log, Throwable e, String... description) {
        Logger l = LoggerFactory.getLogger(clazz); 
        l.error(log, e);
        for (String s : description) {
            l.error(s);
        }
    }
}
