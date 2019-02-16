/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;
import diarsid.beam.core.base.control.flow.ValueFlowDone;


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
        ValueFlow<Location> flow = this.locationsKeeper.findLocation(initiator, command);        
        super.reportValueFlow(initiator, flow, "location not found");
    }
    
    void editLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.locationsKeeper.editLocation(initiator, command);
        super.reportVoidFlow(initiator, flow, "done!");
    }
    
    void createLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.locationsKeeper.createLocation(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");        
    }
    
    void removeLocationAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.locationsKeeper.removeLocation(initiator, command);
        super.reportVoidFlow(initiator, flow, "removed.");  
    }
    
    void showAllLocations(Initiator initiator) {
        ValueFlow<Message> flow = this.locationsKeeper.findAll(initiator);
        Function<ValueFlowDone<Message>, Message> onSuccess = (success) -> {
            return success.orThrow();
        }; 
        super.reportValueFlow(initiator, flow, onSuccess, "cannot get all Locations.");
    }
}
