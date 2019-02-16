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
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;
import diarsid.beam.core.base.control.flow.ValueFlowDone;

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
    
    void findBatchAndReport(Initiator initiator, ArgumentsCommand command) {
        ValueFlow<Batch> flow = this.batchesKeeper.findBatch(initiator, command);
        Function<ValueFlowDone<Batch>, Message> ifSuccess = (success) -> {
            return success.orThrow().toMessage();
        };
        super.reportValueFlow(initiator, flow, ifSuccess, "batch not found.");
    }
    
    void editBatchAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.batchesKeeper.editBatch(initiator, command);
        super.reportVoidFlow(initiator, flow, "done!");
    }
    
    void createBatchAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.batchesKeeper.createBatch(initiator, command);
        super.reportVoidFlow(initiator, flow, "created!");
    }
    
    void removeBatchAndReport(Initiator initiator, ArgumentsCommand command) {
        VoidFlow flow = this.batchesKeeper.removeBatch(initiator, command);
        super.reportVoidFlow(initiator, flow, "removed.");
    }
    
    void showAllBatches(Initiator initiator) {
        ValueFlow<Message> flow = this.batchesKeeper.findAll(initiator);
        Function<ValueFlowDone<Message>, Message> onSuccess = (success) -> {
            return success.orThrow();
        }; 
        super.reportValueFlow(initiator, flow, onSuccess, "cannot get all Batches.");
    }
}
