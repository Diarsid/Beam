/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface RmiLocationsHandlerInterface extends Remote {    
    
    boolean newLocation(String locationPath, String locationName) throws RemoteException;
    
    List<Location> getAllLocations() throws RemoteException;
    
    List<Location> getLocations(String locationName) throws RemoteException;
    
    boolean editLocationPath(String name, String newPath) throws RemoteException;
    
    boolean deleteLocation(String locationName) throws RemoteException;
}
