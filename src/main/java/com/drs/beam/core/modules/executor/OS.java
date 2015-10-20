/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.executor;

import java.util.List;

import com.drs.beam.core.entities.Location;

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
}
