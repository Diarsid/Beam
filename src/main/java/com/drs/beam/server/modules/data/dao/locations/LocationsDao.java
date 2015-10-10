/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.data.dao.locations;

import java.sql.SQLException;
import java.util.List;

import com.drs.beam.server.entities.location.Location;

/**
 *
 * @author Diarsid
 */
public interface LocationsDao {
    
    public List<Location> getLocationsByName(String locationName) throws SQLException;
    public List<Location> getLocationsByNameParts(String[] locationNameParts) throws SQLException;
    
    public void saveNewLocation(Location location) throws SQLException;
    
    public boolean removeLocation(String LocationName) throws SQLException;
        
    public List<Location> getAllLocations() throws SQLException;
}
