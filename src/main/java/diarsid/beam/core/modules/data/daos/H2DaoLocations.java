/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;

import java.util.List;

import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.data.DaoLocations;


public class H2DaoLocations implements DaoLocations {
    
    public H2DaoLocations() {
    }

    @Override
    public List<Location> getLocationsByName(String locationName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Location> getLocationsByNameParts(List<String> locationNameParts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveNewLocation(Location location) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeLocation(String locationName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editLocationPath(String locationName, String newPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean editLocationName(String locationName, String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean replaceInPaths(String replaceable, String replacement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Location> getAllLocations() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
