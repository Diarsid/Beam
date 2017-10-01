/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.flow.ValueFlow;

/**
 *
 * @author Diarsid
 */
public interface BatchesKeeper extends NamedEntitiesKeeper {
    
    @Override
    ValueFlow<Batch> findByNamePattern(
            Initiator initiator, String batchNamePattern); 
    
    @Override
    ValueFlow<Batch> findByExactName(
            Initiator initiator, String batchNamePattern); 
    
    List<Batch> getAllBatches(
            Initiator initiator);
    
    ValueFlow<Batch> findBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow createBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow editBatch(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow removeBatch(
            Initiator initiator, ArgumentsCommand command);
    
}
