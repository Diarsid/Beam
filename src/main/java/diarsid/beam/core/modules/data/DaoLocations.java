/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface DaoLocations {
    
    Optional<Location> getLocationByExactName(Initiator initiator, String exactName);
    
    List<Location> getLocationsByNamePattern(
            Initiator initiator, String locationName);
    
    List<Location> getLocationsByNamePatternParts(
            Initiator initiator, List<String> locationNameParts);
    
    boolean saveNewLocation(
            Initiator initiator, Location location);
    
    boolean removeLocation(
            Initiator initiator, String locationName);
    
    boolean editLocationPath(
            Initiator initiator, String locationName, String newPath);
    
    boolean editLocationName(
            Initiator initiator, String locationName, String newName);
    
    boolean replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
