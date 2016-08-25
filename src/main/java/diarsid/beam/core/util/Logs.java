/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Diarsid
 */
public class Logs {
    
    private final static Logger DEBUGGER;    
    static {
        DEBUGGER = LoggerFactory.getLogger("debugger");
    }
    
    private Logs() {
    }
    
    public static void debug(String log) {
        DEBUGGER.debug(log); 
    }
    
    public static void log(Class clazz, String log) {
        LoggerFactory.getLogger(clazz).info(log);
    }
    
    public static void logError(Class clazz, String log, String... description) {
        Logger l = LoggerFactory.getLogger(clazz); 
        l.error(log);
        for (String s : description) {
            l.error(s);
        }
    }
    
    public static void logError(Class clazz, String log, Exception e, String... description) {
        Logger l = LoggerFactory.getLogger(clazz); 
        l.error(log, e);
        for (String s : description) {
            l.error(s);
        }
    }
}
