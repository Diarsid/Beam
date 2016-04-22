/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.modules.data.HandlerLocations;
import diarsid.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;

/**
 *
 * @author Diarsid
 */
class RmiAdapterForLocationsHandler implements RmiLocationsHandlerInterface {
    
    private final HandlerLocations locationHandler;
 
    RmiAdapterForLocationsHandler(HandlerLocations locHandler) {
        this.locationHandler = locHandler;
    }
    @Override
    public boolean newLocation(String locationPath, String locationName) 
            throws RemoteException {
        
        return this.locationHandler.newLocation(locationPath, locationName);
    }
    
    @Override
    public List<Location> getAllLocations() throws RemoteException{
        return this.locationHandler.getAllLocations();
    }
    
    @Override
    public List<Location> getLocations(String locationName) 
            throws RemoteException {
        
        return this.locationHandler.getLocations(locationName);        
    }
    
    @Override
    public boolean editLocationPath(String name, String newPath) 
            throws RemoteException {
        
        return this.locationHandler.editLocationPath(name, newPath);
    }
    
    @Override
    public boolean deleteLocation(String locationName) throws RemoteException {
        return this.locationHandler.deleteLocation(locationName); 
    }
}
