/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.config.reader;

/**
 *
 * @author Diarsid
 */
public interface ConfigReader {
    public  int      getBeamPort();
    public  String   getBeamHost();
    public  int      getSystemConsolePort();
    public  String   getSystemConsoleHost();
    public  String   getTaskManagerName();
    public  String   getExecutorName();
    public  String   getAccessName();
    public  String   getSystemConsoleName();
    public  String   getCoreDBDriver();
    public  String   getCoreDBURL();
    public  String   getCoreDBName();
    public  String   getProgramsLocation();
    public  String   getImagesLocation();
    public  String   getLibrariesLocation();
    public  String   getDbDriverJar();
    public  String   getJvmOptionsForBeam();
    public  String   getJvmOptionsForSystemConsole();
    
    public static ConfigReader getReader(){
        // currently using ConfigReader implementation.
        return ConfigReaderImpl.getReader();
    }
}
