/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.data.dao.locations;

import com.drs.beam.modules.data.DataManager;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
public interface LocationsDao {
    
    public Map<String, String> getLocationsByName(String locationName);
    public Map<String, String> getLocationsByNameParts(String[] locationNameParts);
    
    public void saveNewLocation(String locationPath, String locationName);
    
    public boolean removeLocation(String LocationName);
        
    public Map<String, String> getLocations();
}
