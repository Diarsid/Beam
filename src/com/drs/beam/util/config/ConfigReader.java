/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.config;

/**
 *
 * @author Diarsid
 */
public interface ConfigReader {
    public  int      getOrganizerPort();
    public  String   getOrganizerHost();
    public  int      getConsolePort();
    public  String   getConsoleHost();
    public  String   getTaskManagerName();
    public  String   getOSExecutorName();
    public  String   getOrgIOName();
    public  String   getConsoleName();
    public  String   getCoreDBDriver();
    public  String   getCoreDBURL();
    public  String   getCoreDBName();
    
    public static ConfigReader getReader(){
        // currently using ConfigReader implementation.
        return new ConfigReaderJAXP();
    }
}
