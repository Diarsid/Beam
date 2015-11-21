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
    
    public List<Location> getLocationsByName(String locationName);
    public List<Location> getLocationsByNameParts(String[] locationNameParts);
    
    public void saveNewLocation(Location location);
    
    public boolean removeLocation(String LocationName);
        
    public List<Location> getAllLocations();
}
