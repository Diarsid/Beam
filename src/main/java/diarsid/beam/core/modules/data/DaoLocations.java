/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.entities.local.Location;

/**
 *
 * @author Diarsid
 */
public interface DaoLocations {
    
    List<Location> getLocationsByName(String locationName);
    List<Location> getLocationsByNameParts(String[] locationNameParts);
    
    boolean saveNewLocation(Location location);
    
    boolean removeLocation(String locationName);
    
    boolean editLocationPath(String locationName, String newPath);
            
    List<Location> getAllLocations();
}
