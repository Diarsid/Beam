/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;

/**
 *
 * @author Diarsid
 */
public interface LocationSubPathKeeper {
    
    List<Variant> findAllLocationSubPaths(
            Initiator initiator, Location location, String pattern);
    
    ValueFlow<LocationSubPath> findLocationSubPath(
            Initiator initiator, Location location, String pattern);
    
    ValueFlow<LocationSubPath> findLocationSubPath(
            Initiator initiator, String pattern);
    
}
