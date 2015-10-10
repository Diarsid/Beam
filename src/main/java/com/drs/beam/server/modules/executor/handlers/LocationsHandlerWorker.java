/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.modules.executor.handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.data.dao.locations.LocationsDao;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
public class LocationsHandlerWorker implements LocationsHandler{
    // Fields =============================================================================
    
    private final InnerIOModule ioEngine;
    private final LocationsDao dao;
    // Constructors =======================================================================

    public LocationsHandlerWorker(LocationsDao dao, InnerIOModule io) {
        this.ioEngine = io;
        this.dao = dao;
    }
    
    // Methods ============================================================================
    
    @Override
    public void newLocation(String locationPath, String locationName){
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        try{
            this.dao.saveNewLocation(new Location(locationName, locationPath));   
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")){
                this.ioEngine.reportMessage("Such location name already exists.");
            } else {
                this.ioEngine.reportException(e, "SQLException: save location.");
            }
        } 
    }    
    
    @Override
    public List<Location> getAllLocations(){
        try{
            return this.dao.getAllLocations();
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get all locations.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Location> getLocations(String locationName){
        locationName = locationName.trim().toLowerCase();        
        try{
            if (locationName.contains("-")){
                return this.dao.getLocationsByNameParts(locationName.split("-"));            
            } else {
                return this.dao.getLocationsByName(locationName);            
            }
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get locations by name.");
            return Collections.emptyList();
        }
    }
    
    @Override
    public Location getLocation(String locationName){
        locationName = locationName.trim().toLowerCase();
        List<Location> foundLocations;
        try{
            if (locationName.contains("-")){
                foundLocations = this.dao.getLocationsByNameParts(locationName.split("-"));            
            } else {
                foundLocations = this.dao.getLocationsByName(locationName);            
            }
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: get locations by name.");
            return null;
        }
        return this.resolveMultipleLocations(foundLocations);
    }
    
    @Override
    public boolean deleteLocation(String locationName){        
        locationName = locationName.trim().toLowerCase();        
        try{
            return this.dao.removeLocation(locationName);
        } catch (SQLException e) {
            this.ioEngine.reportException(e, "SQLException: remove location.");
            return false;
        }
    }
    
    private Location resolveMultipleLocations(List<Location> foundLocations){
        if (foundLocations.size() < 1){
            this.ioEngine.reportMessage("Couldn`t find such location.");
            return null;
        } else if (foundLocations.size() == 1){
            return foundLocations.get(0);
        } else {
            List<String> locationNames = new ArrayList();
            for (Location loc : foundLocations){
                locationNames.add(loc.getName());
            }
            int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several locations:", 
                    locationNames);
            if (varNumber < 0){
                return null;
            } else {
                return foundLocations.get(varNumber-1);
            }            
        }
    }
}
