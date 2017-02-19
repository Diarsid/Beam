/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import java.util.function.Function;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.toMessage;

import diarsid.beam.core.base.control.flow.ReturnOperation;
import diarsid.beam.core.base.control.flow.OkReturnOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;

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
        ReturnOperation<Batch> flow = this.batchesKeeper.findBatch(initiator, command);
        Function<OkReturnOperation, Message> ifSuccess = (success) -> {
            return toMessage((Batch) success.getOrThrow());
        };
        super.reportReturnOperationFlow(initiator, flow, ifSuccess, "batch not found.");
    }
    
    void editBatchAndReport(Initiator initiator, SingleStringCommand command) {
        VoidOperation flow = this.batchesKeeper.editBatch(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "done!");
    }
    
    void createBatchAndReport(Initiator initiator, SingleStringCommand command) {
        VoidOperation flow = this.batchesKeeper.createBatch(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "created!");
    }
    
    void removeBatchAndReport(Initiator initiator, SingleStringCommand command) {
        VoidOperation flow = this.batchesKeeper.removeBatch(initiator, command);
        super.reportVoidOperationFlow(initiator, flow, "removed.");
    }
}
