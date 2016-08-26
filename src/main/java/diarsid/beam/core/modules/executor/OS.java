/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor;

import java.util.List;

import diarsid.beam.core.entities.local.Location;
import diarsid.beam.core.modules.executor.workflow.OperationResult;

/**
 *
 * 
 */
public interface OS {
    
    OperationResult openLocation(Location location);
    
    OperationResult openFileInLocation(String file, Location location);
    
    OperationResult openFileInLocationWithProgram(
            String file, Location location, String program);
    
    OperationResult runProgram(String program);
    
    List<String> getLocationContent(Location location); 
    
    OperationResult openUrlWithDefaultBrowser(String name);
    
    OperationResult openUrlWithGivenBrowser(
            String urlAddress, String browserName);
    
    void createAndOpenTxtFileIn(String name, Location location);
}
