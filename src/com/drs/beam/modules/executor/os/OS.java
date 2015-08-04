/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.executor.os;

import com.drs.beam.modules.io.BeamIO;

/**
 *
 * 
 */
public interface OS {
    boolean ifDirectoryExists(String location);
    
    void openLocation(String location);
    void openFileInLocation(String file, String location);
    
    void openFileInLocationWithProgram(String file, String location, String program);
    void runProgram(String program);
    
    static OS getOS(){
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("win")){
            return new OSWindows();
        } else if (systemName.contains("x")) {
            // Program does not have OSUnix implementation for working under this OS.
            // Terminates program
            BeamIO.getInnerIO().informAboutError("Program does not have OSUnix implementation yet.", true);
            return null;
        } else {
            // Some error occured or there is unknown OS.
            // Terminates program
            BeamIO.getInnerIO().informAboutError("Unsupported or unknown OS.", true);
            return null;
        }     
    }
}
