/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.control.flow.OperationResult;

/**
 *
 * 
 */
public interface OS {
    
    void openLocation(Location location);
    
    void openFileInLocation(String file, Location location);
        
    void runProgram(String program);
    
    void runMarkedProgram(String program, String mark);
    
    List<String> listContentIn(Location location, int depth);
    
    List<String> listContentIn(Location location, String relativePath, int depth); 
    
    OperationResult openUrlWithDefaultBrowser(String name);
        
    void createAndOpenTxtFileIn(String name, Location location);
}
