/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoLocations extends BeamCommonResponsiveDao<DaoLocations> {

    ResponsiveDaoLocations(DaoLocations dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public boolean isNameFree(Initiator initiator, String exactName) {
        try {
            return super.dao().isNameFree(exactName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public Optional<Location> getLocationByExactName(Initiator initiator, String exactName) {
        try {
            return super.dao().getLocationByExactName(exactName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<Location> getLocationByPath(Initiator initiator, String path) {
        try {
            return super.dao().getLocationByPath(path);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        } 
    }
    
    public List<Location> getLocationsByNamePattern(Initiator initiator, String locationName) {
        try {
            return super.dao().getLocationsByNamePattern(locationName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public boolean saveNewLocation(Initiator initiator, Location location) {
        try {
            return super.dao().saveNewLocation(location);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean removeLocation(Initiator initiator, String locationName) {
        try {
            return super.dao().removeLocation(locationName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editLocationPath(Initiator initiator, String locationName, String newPath) {
        try {
            return super.dao().editLocationPath(locationName, newPath);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editLocationName(Initiator initiator, String locationName, String newName) {
        try {
            return super.dao().editLocationName(locationName, newName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean replaceInPaths(Initiator initiator, String replaceable, String replacement) {
        try {
            return super.dao().replaceInPaths(replaceable, replacement);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
            
    public List<Location> getAllLocations(Initiator initiator) {
        try {
            return super.dao().getAllLocations();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
}
