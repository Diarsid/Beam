/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.dao.locations;

import java.util.List;

import com.drs.beam.server.entities.location.Location;

/**
 *
 * @author Diarsid
 */
public interface LocationsDao {
    
    public List<Location> getLocationsByName(String locationName);
    public List<Location> getLocationsByNameParts(String[] locationNameParts);
    
    public void saveNewLocation(Location location);
    
    public boolean removeLocation(String LocationName);
        
    public List<Location> getAllLocations();
}
