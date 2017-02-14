/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.EditEntityCommand;
import diarsid.beam.core.base.control.io.commands.FindEntityCommand;
import diarsid.beam.core.base.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.base.control.io.commands.creation.CreateLocationCommand;
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
            Initiator initiator, FindEntityCommand command);
    
    List<Location> getLocationsByNamePattern(
            Initiator initiator, String namePattern);
    
    boolean createLocation(
            Initiator initiator, CreateLocationCommand command);
    
    boolean removeLocation(
            Initiator initiator, RemoveEntityCommand command);
    
    boolean editLocation(
            Initiator initiator, EditEntityCommand command);
    
    boolean replaceInPaths(
            Initiator initiator, String replaceable, String replacement);
            
    List<Location> getAllLocations(
            Initiator initiator);
}
