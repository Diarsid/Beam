/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.executor.os;

import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * 
 */
public interface OS {
    
    boolean checkIfDirectoryExists(String directory);
    
    void openLocation(Location location);
    void openFileInLocation(String file, Location location);
    
    void openFileInLocationWithProgram(String file, Location location, String program);
    void runProgram(String program);
    
    List<String> getLocationContent(Location location);
    
    static OS getOS(InnerIOModule io){
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            return new OSWindows(io);
        } else if (systemName.contains("x")) {
            // Program does not have OSUnix implementation for working under this OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Program does not have OSUnix implementation yet.",
                    "Programm will be closed.");
            return null;
        } else {
            // Some error occured or there is unknown OS.
            // Terminates program
            io.reportErrorAndExitLater(
                    "Unsupported or unknown OS.", 
                    "Programm will be closed.");
            return null;
        }     
    }
}
