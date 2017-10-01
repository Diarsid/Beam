/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.flow.ValueFlow;

/**
 *
 * @author Diarsid
 */
public interface LocationsKeeper extends NamedEntitiesKeeper {
    
    @Override
    ValueFlow<Location> findByExactName(
            Initiator initiator, String exactName);
    
    @Override
    ValueFlow<Location> findByNamePattern(
            Initiator initiator, String locationNamePattern);
    
    List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern);
    
    ValueFlow<Location> findLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow createLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow removeLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow editLocation(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
