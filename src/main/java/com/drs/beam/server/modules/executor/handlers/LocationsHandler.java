/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.server.modules.executor.handlers;

import java.util.List;

import com.drs.beam.server.entities.location.Location;

/**
 *
 * @author Diarsid
 */
public interface LocationsHandler {
    
    void newLocation(String locationPath, String locationName);
    
    List<Location> getAllLocations();
    
    List<Location> getLocations(String locationName);
    
    Location getLocation(String locationName);
    
    boolean deleteLocation(String locationName);
}
