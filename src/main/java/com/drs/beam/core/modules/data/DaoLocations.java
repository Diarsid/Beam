/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.util.List;

import com.drs.beam.core.entities.Location;

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
