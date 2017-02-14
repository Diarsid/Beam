/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.base.control.io.commands.EditEntityCommand;
import diarsid.beam.core.base.control.io.commands.FindEntityCommand;
import diarsid.beam.core.base.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.DomainToMessageConversion.toMessage;

/**
 *
 * @author Diarsid
 */
class CliAdapterForBatchesKeeper {
    
    private final BatchesKeeper batchesKeeper;
    private final InnerIoEngine ioEngine;
    
    CliAdapterForBatchesKeeper(BatchesKeeper batchesKeeper, InnerIoEngine ioEngine) {
        this.batchesKeeper = batchesKeeper;
        this.ioEngine = ioEngine;
    }
    
    void findBatchAndReport(Initiator initiator, FindEntityCommand command) {
        Optional<Batch> possibleBatch = this.batchesKeeper.findBatch(initiator, command);
        if ( possibleBatch.isPresent() ) {
            this.ioEngine.reportMessage(initiator, toMessage(possibleBatch.get()));
        } else {
            this.ioEngine.report(initiator, "not found.");
        }
    }
    
    void editBatchAndReport(Initiator initiator, EditEntityCommand command) {
        if ( this.batchesKeeper.editBatch(initiator, command) ) {
            this.ioEngine.report(initiator, "done!");
        }
    }
    
    void createBatchAndReport(Initiator initiator, CreateEntityCommand command) {
        if ( this.batchesKeeper.createBatch(initiator, command) ) {
            this.ioEngine.report(initiator, "created!");
        }
    }
    
    void removeBatchAndReport(Initiator initiator, RemoveEntityCommand command) {
        if ( this.batchesKeeper.removeBatch(initiator, command) ) {
            this.ioEngine.report(initiator, "removed.");
        }
    }
}
