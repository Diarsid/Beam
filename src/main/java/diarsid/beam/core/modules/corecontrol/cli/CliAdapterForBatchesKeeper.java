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
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.DomainToMessageConversion.toMessage;

/**
 *
 * @author Diarsid
 */
class CliAdapterForBatchesKeeper extends AbstractCliAdapter {
    
    private final BatchesKeeper batchesKeeper;
    
    CliAdapterForBatchesKeeper(BatchesKeeper batchesKeeper, InnerIoEngine ioEngine) {
        super(ioEngine);
        this.batchesKeeper = batchesKeeper;
    }
    
    void findBatchAndReport(Initiator initiator, SingleStringCommand command) {
        Optional<Batch> possibleBatch = this.batchesKeeper.findBatch(initiator, command);
        if ( possibleBatch.isPresent() ) {
            super.report(initiator, toMessage(possibleBatch.get()));
        } else {
            super.report(initiator, "not found.");
        }
    }
    
    void editBatchAndReport(Initiator initiator, SingleStringCommand command) {
        OperationFlow flow = this.batchesKeeper.editBatch(initiator, command);
        super.reportOperationFlow(initiator, flow, "done!");
    }
    
    void createBatchAndReport(Initiator initiator, SingleStringCommand command) {
        OperationFlow flow = this.batchesKeeper.createBatch(initiator, command);
        super.reportOperationFlow(initiator, flow, "created!");
    }
    
    void removeBatchAndReport(Initiator initiator, SingleStringCommand command) {
        OperationFlow flow = this.batchesKeeper.removeBatch(initiator, command);
        super.reportOperationFlow(initiator, flow, "removed.");
    }
}
