/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.DomainToMessageConversion.toMessage;


/**
 *
 * @author Diarsid
 */
public class CliAdapterForLocationsKeeper extends AbstractCliAdapter {
    
    private final LocationsKeeper locationsKeeper;
    
    public CliAdapterForLocationsKeeper(
            LocationsKeeper locationsKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.locationsKeeper = locationsKeeper;
    }
    
    void findLocationAndReport(Initiator initiator, SingleStringCommand command) {
        Optional<Location> location = this.locationsKeeper.findLocation(initiator, command);
        if ( location.isPresent() ) {
            super.report(initiator, toMessage(location.get()));
        } else {
            super.report(initiator, "not found.");
        }        
    }
    
    void editLocationAndReport(Initiator initiator, SingleStringCommand command) {
        OperationFlow flow = this.locationsKeeper.editLocation(initiator, command);
        super.reportOperationFlow(initiator, flow, "done!");
    }
    
    void createLocationAndReport(Initiator initiator, MultiStringCommand command) {
        OperationFlow flow = this.locationsKeeper.createLocation(initiator, command);
        super.reportOperationFlow(initiator, flow, "created!");        
    }
    
    void removeLocationAndReport(Initiator initiator, SingleStringCommand command) {
        OperationFlow flow = this.locationsKeeper.removeLocation(initiator, command);
        super.reportOperationFlow(initiator, flow, "created!");  
    }
}
