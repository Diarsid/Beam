/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface DaoLocations extends Dao {
    
    boolean isNameFree(
            String exactName) throws DataExtractionException;
    
    Optional<Location> getLocationByExactName(
            String exactName) throws DataExtractionException;
    
    Optional<Location> getLocationByPath(
            String path) throws DataExtractionException;
    
    List<Location> getLocationsByNamePattern(
            String locationName) throws DataExtractionException;
    
    boolean saveNewLocation(
            Location location) throws DataExtractionException;
    
    boolean removeLocation(
            String locationName) throws DataExtractionException;
    
    boolean editLocationPath(
            String locationName, String newPath) throws DataExtractionException;
    
    boolean editLocationName(
            String locationName, String newName) throws DataExtractionException;
    
    boolean replaceInPaths(
            String replaceable, String replacement) throws DataExtractionException;
            
    List<Location> getAllLocations() throws DataExtractionException;
}
