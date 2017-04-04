/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface BatchesKeeper extends NamedEntitiesKeeper {
    
    @Override
    Optional<Batch> findByNamePattern(
            Initiator initiator, String batchNamePattern); 
    
    @Override
    Optional<Batch> findByExactName(
            Initiator initiator, String batchNamePattern); 
    
    List<Batch> getAllBatches(
            Initiator initiator);
    
    ValueOperation<Batch> findBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation createBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation removeBatch(
            Initiator initiator, ArgumentsCommand command);
    
}
