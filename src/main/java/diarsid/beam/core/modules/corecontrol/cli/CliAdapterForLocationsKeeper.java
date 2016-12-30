/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.modules.domain.keepers.LocationsKeeper;

import static diarsid.beam.core.control.io.base.DomainToMessageConversion.toMessage;


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
    
    void findLocationsAndReport(Initiator initiator, FindEntityCommand command) {
        this.ioEngine.reportMessage(
                initiator, 
                toMessage(this.locationsKeeper.getLocations(initiator, command)));
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
