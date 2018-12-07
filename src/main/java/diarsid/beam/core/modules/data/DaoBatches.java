/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface DaoBatches extends Dao {
    
    boolean isNameFree(String exactName) 
            throws DataExtractionException;
    
    List<String> getBatchNamesByNamePattern(String batchName) 
            throws DataExtractionException;
    
    Optional<Batch> getBatchByExactName(String name) 
            throws DataExtractionException;
    
    boolean saveBatch(Batch batch) 
            throws DataExtractionException;
    
    boolean removeBatch(String batchName) 
            throws DataExtractionException;
    
    boolean editBatchName(String batchName, String newName) 
            throws DataExtractionException;
    
    boolean editBatchCommands(String batchName, List<ExecutorCommand> newCommands) 
            throws DataExtractionException;
    
    boolean editBatchOneCommand(String batchName, int commandOrder, ExecutorCommand newCommand) 
            throws DataExtractionException;
    
    List<Batch> getAllBatches() 
            throws DataExtractionException;
    
}
