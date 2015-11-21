/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.modules.data.DaoLocations;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;

/**
 *
 * @author Diarsid
 */
class RmiLocationsHandlerAdapter implements RmiLocationsHandlerInterface{
    // Fields =============================================================================
    
    private final DaoLocations dao;

    // Constructors =======================================================================
 
    RmiLocationsHandlerAdapter(DaoLocations dao) {
        this.dao = dao;
    }
    
    // Methods ============================================================================

    @Override
    public void newLocation(String locationPath, String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        locationPath = locationPath.trim().toLowerCase();
        this.dao.saveNewLocation(new Location(locationName, locationPath)); 
    }
    
    @Override
    public List<Location> getAllLocations() throws RemoteException{
        return this.dao.getAllLocations();
    }
    
    @Override
    public List<Location> getLocations(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        if (locationName.contains("-")){
            return this.dao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            return this.dao.getLocationsByName(locationName);            
        }
    }
    
    @Override
    public boolean deleteLocation(String locationName) throws RemoteException{
        locationName = locationName.trim().toLowerCase();
        return this.dao.removeLocation(locationName);
    }

}
