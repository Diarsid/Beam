/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface BatchesKeeper {
    
    Optional<Batch> getBatchByNamePattern(
            Initiator initiator, String batchNamePattern);
    
    Optional<Batch> findBatch(
            Initiator initiator, SingleStringCommand command);
    
    List<Batch> getAllBatches(
            Initiator initiator);
    
    OperationFlow createBatch(
            Initiator initiator, SingleStringCommand command);
    
    OperationFlow editBatch(
            Initiator initiator, SingleStringCommand command);
    
    OperationFlow removeBatch(
            Initiator initiator, SingleStringCommand command);
    
}
