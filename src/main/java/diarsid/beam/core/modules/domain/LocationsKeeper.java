/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domain;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;

/**
 *
 * @author Diarsid
 */
public interface LocationsKeeper {
    
    Optional<Location> getLocation(
            Initiator initiator, FindEntityCommand command);
    
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
