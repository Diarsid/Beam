/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.EditEntityCommand;
import diarsid.beam.core.base.control.io.commands.FindEntityCommand;
import diarsid.beam.core.base.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.base.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.DomainToMessageConversion.toMessage;


/**
 *
 * @author Diarsid
 */
public class CliAdapterForLocationsKeeper {
    
    private final LocationsKeeper locationsKeeper;
    private final InnerIoEngine ioEngine;
    
    public CliAdapterForLocationsKeeper(
            LocationsKeeper locationsKeeper, InnerIoEngine ioEngine) {
        this.locationsKeeper = locationsKeeper;
        this.ioEngine = ioEngine;
    }
    
    void findLocationAndReport(Initiator initiator, FindEntityCommand command) {
        Optional<Location> location = this.locationsKeeper.findLocation(initiator, command);
        if ( location.isPresent() ) {
            this.ioEngine.reportMessage(initiator, toMessage(location.get()));
        } else {
            this.ioEngine.report(initiator, "not found.");
        }        
    }
    
    void editLocationAndReport(Initiator initiator, EditEntityCommand command) {
        if ( this.locationsKeeper.editLocation(initiator, command) ) {
            this.ioEngine.report(initiator, "done!");
        }
    }
    
    void createLocationAndReport(Initiator initiator, CreateLocationCommand command) {
        if ( this.locationsKeeper.createLocation(initiator, command) ) {
            this.ioEngine.report(initiator, "created!");
        }
    }
    
    void removeLocationAndReport(Initiator initiator, RemoveEntityCommand command) {
        if ( this.locationsKeeper.removeLocation(initiator, command) ) {
            this.ioEngine.report(initiator, "removed.");
        }
    }
}
