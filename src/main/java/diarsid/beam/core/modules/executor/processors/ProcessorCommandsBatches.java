/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

import old.diarsid.beam.core.entities.OldBatch;

/**
 *
 * @author Diarsid
 */
public interface ProcessorCommandsBatches {

    boolean deleteBatch(String batchName);

    List<OldBatch> getAllBatches();

    OldBatch getBatch(String name);

    List<OldBatch> getBatches(String batchName);

    void newBatch(List<String> commands, String batchName);    
}
