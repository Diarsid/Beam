/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface DaoLocations {
    
    List<Location> getLocationsByName(String locationName);
    
    List<Location> getLocationsByNameParts(List<String> locationNameParts);
    
    boolean saveNewLocation(Location location);
    
    boolean removeLocation(String locationName);
    
    boolean editLocationPath(String locationName, String newPath);
    
    boolean editLocationName(String locationName, String newName);
    
    boolean replaceInPaths(String replaceable, String replacement);
            
    List<Location> getAllLocations();
}
