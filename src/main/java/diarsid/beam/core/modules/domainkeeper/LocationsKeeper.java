/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface LocationsKeeper {
    
    Optional<Location> getLocationByExactName(
            Initiator initiator, String exactName);
    
    Optional<Location> getLocationByNamePattern(
            Initiator initiator, String locationNamePattern);
    
    Optional<Location> findLocation(
            Initiator initiator, SingleStringCommand command);
    
    List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern);
    
    OperationFlow createLocation(
            Initiator initiator, MultiStringCommand command);
    
    OperationFlow removeLocation(
            Initiator initiator, SingleStringCommand command);
    
    OperationFlow editLocation(
            Initiator initiator, SingleStringCommand command);
    
    OperationFlow replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
