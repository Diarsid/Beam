/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

/**
 *
 * @author Diarsid
 */
public interface DaoLocationSubPaths extends Dao {
    
    List<LocationSubPath> getSubPathesByPattern(Location location, String pattern) throws DataExtractionException;
    
    List<LocationSubPath> getSubPathesByPattern(String pattern) throws DataExtractionException;
    
}
