/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.modules.executor.entities.StoredCommandsBatch;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsBatches {
    
    public List<StoredCommandsBatch> getBatchesByName(String commandName);
    
    public void saveNewBatch(StoredCommandsBatch command);
    
    public boolean removeBatch(String commandName);
    
    public List<StoredCommandsBatch> getAllBatches();
}
