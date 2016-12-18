/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

import diarsid.beam.core.domain.entities.Batch;

/**
 *
 * @author Diarsid
 */
public interface ProcessorCommandsBatches {

    boolean deleteBatch(String batchName);

    List<Batch> getAllBatches();

    Batch getBatch(String name);

    List<Batch> getBatches(String batchName);

    void newBatch(List<String> commands, String batchName);    
}
