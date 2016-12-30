/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules.data.daos;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface DaoLocations {

    boolean editLocationPath(String locationName, String newPath);

    List<Location> getAllLocations();

    List<Location> getLocationsByName(String locationName);

    List<Location> getLocationsByNameParts(List<String> locationNameParts);

    boolean removeLocation(String locationName);

    // Methods ============================================================================
    boolean saveNewLocation(Location location);
    
}
