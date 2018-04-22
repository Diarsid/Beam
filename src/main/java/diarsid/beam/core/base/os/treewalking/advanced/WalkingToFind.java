/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import diarsid.beam.core.application.environment.Catalog;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

/**
 *
 * @author Diarsid
 */
public interface WalkingToFind {
    
    WalkingInPlace in(String where);
    
    WalkingInPlace in(Catalog catalog);
    
    WalkingInPlace in(Location location);
    
    WalkingInPlace in(LocationSubPath locationSubPath);
    
    WalkingToFind withMaxDepthOf(int maxDepth);
}
