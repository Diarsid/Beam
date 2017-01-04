/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.modules.data;

import java.util.List;

import old.diarsid.beam.core.entities.OldBatch;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsBatches {
    
    public List<OldBatch> getBatchesByName(String commandName);
    
    public void saveNewBatch(OldBatch command);
    
    public boolean removeBatch(String commandName);
    
    public List<OldBatch> getAllBatches();
}
