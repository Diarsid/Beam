/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.base.control.flow.ReturnOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;

/**
 *
 * @author Diarsid
 */
public interface LocationsKeeper {
    
    Optional<Location> getLocationByExactName(
            Initiator initiator, String exactName);
    
    Optional<Location> getLocationByNamePattern(
            Initiator initiator, String locationNamePattern);
    
    List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern);
    
    ReturnOperation<Location> findLocation(
            Initiator initiator, SingleStringCommand command);
    
    VoidOperation createLocation(
            Initiator initiator, MultiStringCommand command);
    
    VoidOperation removeLocation(
            Initiator initiator, SingleStringCommand command);
    
    VoidOperation editLocation(
            Initiator initiator, SingleStringCommand command);
    
    VoidOperation replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
