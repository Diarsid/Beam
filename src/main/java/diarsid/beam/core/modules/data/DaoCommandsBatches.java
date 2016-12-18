/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsBatches {
    
    public List<Batch> getBatchesByName(String commandName);
    
    public void saveNewBatch(Batch command);
    
    public boolean removeBatch(String commandName);
    
    public List<Batch> getAllBatches();
}
