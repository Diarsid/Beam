/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.List;

import diarsid.beam.core.entities.Location;

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
    
    void openUrlWithDefaultBrowser(String name);
    
    void openUrlWithGivenBrowser(String urlAddress, String browserName);
    
    void createAndOpenTxtFileIn(String name, Location location);
}
