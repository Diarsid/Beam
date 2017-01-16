/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface BatchesKeeper {
    
    Optional<Batch> getBatchByNamePattern(
            Initiator initiator, String batchNamePattern);
    
    Optional<Batch> findBatch(
            Initiator initiator, FindEntityCommand command);
    
    List<Batch> getAllBatches(
            Initiator initiator);
    
    boolean createBatch(
            Initiator initiator, CreateEntityCommand command);
    
    boolean editBatch(
            Initiator initiator, EditEntityCommand command);
    
    boolean removeBatch(
            Initiator initiator, RemoveEntityCommand command);
    
}
