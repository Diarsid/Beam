/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.util.List;

import diarsid.beam.core.application.environment.Catalog;
import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface WalkingToFind {
    
    WalkingInPlace in(String where);
    
    WalkingInPlace in(Catalog catalog);
    
    WalkingInPlace in(Location location);
    
    WalkingToFind withMinDepthOf(int minDepth);
    
    WalkingToFind withMaxDepthOf(int maxDepth);
    
    WalkingToFind withPredefined(List<Variant> predefinedVariants);
}
