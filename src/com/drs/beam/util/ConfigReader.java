/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.util;

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
