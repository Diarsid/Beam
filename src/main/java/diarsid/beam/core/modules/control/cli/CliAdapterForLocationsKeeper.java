/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.OkReturnOperation;
import diarsid.beam.core.base.control.flow.ReturnOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.toMessage;


/**
 *
 * @author Diarsid
 */
class CliAdapterForLocationsKeeper extends AbstractCliAdapter {
    
    private final LocationsKeeper locationsKeeper;
    
    CliAdapterForLocationsKeeper(
            LocationsKeeper locationsKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.locationsKeeper = locationsKeeper;
    }
    
    void findLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        ReturnOperation<Location> flow = this.locationsKeeper.findLocation(initiator, command);
        Function<OkReturnOperation, Message> ifSuccess = (success) -> {
            return toMessage((Location) success.getOrThrow());
        };
        super.reportReturnOperationFlow(initiator, flow, ifSuccess, "location not found");
    }
    
    void editLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.locationsKeeper.editLocation(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "done!");
    }
    
    void createLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.locationsKeeper.createLocation(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");        
    }
    
    void removeLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidOperation flow = this.locationsKeeper.removeLocation(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");  
    }
}
