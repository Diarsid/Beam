/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoBatches {
    
    boolean isNameFree(Initiator initiator, String exactName);
    
    List<String> getBatchNamesByNamePattern(
            Initiator initiator, String batchName);
    
    List<String> getBatchNamesByNamePatternParts(
            Initiator initiator, List<String> batchNameParts);
    
    Optional<Batch> getBatchByExactName(Initiator initiator, String name);
    
    boolean saveBatch(
            Initiator initiator, Batch batch);
    
    boolean removeBatch(
            Initiator initiator, String batchName);
    
    boolean editBatchName(
            Initiator initiator, String batchName, String newName);
    
    boolean editBatchCommands(
            Initiator initiator, String batchName, List<ExtendableCommand> newCommands);
    
    boolean editBatchOneCommand(
            Initiator initiator, String batchName, int commandOrder, ExtendableCommand newCommand);
    
    List<Batch> getAllBatches(
            Initiator initiator);
}
