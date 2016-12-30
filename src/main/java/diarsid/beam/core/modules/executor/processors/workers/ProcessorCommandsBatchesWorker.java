/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import old.diarsid.beam.core.modules.IoInnerModule;

import old.diarsid.beam.core.modules.data.DaoCommandsBatches;

import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.executor.processors.ProcessorCommandsBatches;
import diarsid.beam.core.modules.executor.context.ExecutorContext;

/**
 *
 * @author Diarsid
 */
class ProcessorCommandsBatchesWorker implements ProcessorCommandsBatches {
    
    private final IoInnerModule ioEngine;
    private final DaoCommandsBatches batchesDao;
    private final ExecutorContext intellContext;
    
    ProcessorCommandsBatchesWorker(
            IoInnerModule io, 
            DaoCommandsBatches dao, 
            ExecutorContext intell) {
        
        this.ioEngine = io;
        this.batchesDao = dao;
        this.intellContext = intell;
    }    
    
    @Override
    public Batch getBatch(String batchName) {        
        batchName = batchName.trim().toLowerCase();
        List<Batch> foundBatches = 
                this.batchesDao.getBatchesByName(batchName);    
        
        if ( foundBatches.size() < 1 ) {
            this.ioEngine.reportMessage("Couldn`t find such batch.");
            this.intellContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                    "call", batchName);
            return null;
        } else if ( foundBatches.size() == 1 ) {
            return foundBatches.get(0);
        } else {
            List<String> foundBatchesNames = new ArrayList<>();
            for (Batch c : foundBatches) {
                foundBatchesNames.add(c.getName());
            }
            int variant = this.intellContext.resolve(
                    "There are several batches:", 
                    batchName, 
                    foundBatchesNames);
            if ( variant < 0 ) {
                return null;
            } else {
                return foundBatches.get(variant-1);
            }            
        }
    }       
    
    @Override
    public void newBatch(List<String> commands, String batchName) {
        for(int i = 0; i < commands.size(); i++) {
            String s = commands.get(i).trim().toLowerCase();
            if ( s.equals("call") || s.equals("exe") ) {
                this.ioEngine.reportMessage(
                        "'call' and 'exe' commands are not permitted "
                                + "to use in batched commands.",
                        "Calling a batch inside another batch can cause "
                                + "an endless cyclical execution.");
                return;
            }
            commands.set(i, s);
        }
        batchName = batchName.trim().toLowerCase();
        this.batchesDao.saveNewBatch(new Batch(batchName, commands));
    }    
        
    @Override
    public boolean deleteBatch(String batchName) {
        batchName = batchName.trim().toLowerCase();
        return this.batchesDao.removeBatch(batchName);
    }    
    
    @Override
    public List<Batch> getAllBatches() {
        return this.batchesDao.getAllBatches();
    }    
    
    @Override
    public List<Batch> getBatches(String batchName) {
        batchName = batchName.trim().toLowerCase();
        return this.batchesDao.getBatchesByName(batchName);
    }    
}
