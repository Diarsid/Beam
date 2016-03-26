/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.handlers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoLocations;

/**
 *
 * @author Diarsid
 */
class LocationsHandlerWorker implements LocationsHandler {
    
    private final DaoLocations dao;
    private final IoInnerModule ioEngine;
    
    LocationsHandlerWorker(IoInnerModule io, DaoLocations dao) {
        this.dao = dao;
        this.ioEngine = io;
    }
    
    @Override
    public boolean newLocation(String locationPath, String locationName) {
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        if ( this.checkPath(locationPath)) {
            return this.dao.saveNewLocation(new Location(locationName, locationPath)); 
        } else {
            return false;
        }
    }
    
    @Override
    public List<Location> getAllLocations() {
        return this.dao.getAllLocations();
    }
    
    @Override
    public List<Location> getLocations(String locationName) {
        locationName = locationName.trim().toLowerCase();
        if (locationName.contains("-")){
            return this.dao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            return this.dao.getLocationsByName(locationName);            
        }
    }
    
    @Override
    public boolean editLocationPath(String name, String newPath) {
        name = name.trim().toLowerCase();
        newPath = newPath.trim().toLowerCase();
        if ( this.checkPath(newPath)) {
            return this.dao.editLocationPath(name, newPath);
        } else {
            return false;
        }    
    }
    
    @Override
    public boolean deleteLocation(String locationName) {
        locationName = locationName.trim().toLowerCase();
        return this.dao.removeLocation(locationName);
    }
    
    private boolean checkPath(String path) {
        Path dir = Paths.get(path);
        return (Files.exists(dir) && Files.isDirectory(dir));
    }
}
