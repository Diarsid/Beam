/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.executor.os;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;

/**
 *
 * @author Diarsid
 */
public interface OS {
    public boolean ifDirectoryExists(String location);
    
    public void openLocation(String location);
    public void openFileInLocation(String file, String location);
    
    public void openFileInLocationWithProgram(String file, String location, String program);
    public void runProgram(String program);
    
    public static OS getOS(String programs){
        String systemName = System.getProperty("os.name").toLowerCase();
        if (systemName.contains("windows")){
            return new Windows(programs);
        } else {
            // terminates program
            BeamIO.getInnerIO().informAboutError("Executor init error: ", true);
            return null;
        }      
    }
}
