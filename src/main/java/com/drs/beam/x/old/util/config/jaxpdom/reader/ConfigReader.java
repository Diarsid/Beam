/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.x.old.util.config.jaxpdom.reader;

/**
 *
 * @author Diarsid
 */
public interface ConfigReader {
    
    public static ConfigReader getReader(){
        return ConfigReaderImpl.getReader();
    }
    
    int      getBeamPort();
    String   getBeamHost();
    int      getSystemConsolePort();
    String   getSystemConsoleHost();
    String   getTaskManagerName();
    String   getExecutorName();
    String   getAccessName();
    String   getLocationsHandlerName();
    String   getWebPagesHandlerName();
    String   getSystemConsoleName();
    String   getCoreDBDriver();
    String   getCoreDBURL();
    String   getCoreDBName();
    String   getProgramsLocation();
    String   getImagesLocation();
    String   getLibrariesLocation();
    String   getDbDriverJar();
    String   getJvmOptionsForBeam();
    String   getJvmOptionsForSystemConsole();
}
