/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface HandlerLocations {
    
    boolean newLocation(String locationPath, String locationName);
    
    List<Location> getAllLocations();
    
    List<Location> getLocations(String locationName);
    
    boolean editLocationPath(String name, String newPath);
    
    boolean deleteLocation(String locationName);
}
