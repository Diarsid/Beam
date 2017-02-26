/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.base.control.flow.ValueOperation;

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
    
    ValueOperation<Location> findLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation createLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation removeLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
