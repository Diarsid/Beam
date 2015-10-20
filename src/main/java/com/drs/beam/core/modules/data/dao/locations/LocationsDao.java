/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data.dao.locations;

import java.sql.SQLException;
import java.util.List;

import com.drs.beam.core.entities.Location;

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
