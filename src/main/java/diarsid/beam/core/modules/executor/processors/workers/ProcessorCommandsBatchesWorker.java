/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoCommandsBatches;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.entities.StoredCommandsBatch;
import diarsid.beam.core.modules.executor.processors.ProcessorCommandsBatches;

/**
 *
 * @author Diarsid
 */
class ProcessorCommandsBatchesWorker implements ProcessorCommandsBatches {
    
    private final IoInnerModule ioEngine;
    private final DaoCommandsBatches batchesDao;
    private final IntelligentExecutorCommandContext intellContext;
    
    ProcessorCommandsBatchesWorker(
            IoInnerModule io, 
            DaoCommandsBatches dao, 
            IntelligentExecutorCommandContext intell) {
        
        this.ioEngine = io;
        this.batchesDao = dao;
        this.intellContext = intell;
    }    
    
    @Override
    public StoredCommandsBatch getBatch(String name) {        
        name = name.trim().toLowerCase();
        List<StoredCommandsBatch> commands = this.batchesDao.getBatchesByName(name);    
        
        if ( commands.size() < 1 ) {
            this.ioEngine.reportMessage("Couldn`t find such batch.");
            return null;
        } else if ( commands.size() == 1 ) {
            return commands.get(0);
        } else {
            List<String> commandNames = new ArrayList<>();
            for (StoredCommandsBatch c : commands) {
                commandNames.add(c.getName());
            }
            int variant = this.intellContext.resolve(
                    "There are several batches:", 
                    name, 
                    commandNames);
            if ( variant < 0 ) {
                return null;
            } else {
                return commands.get(variant-1);
            }            
        }
    }       
    
    @Override
    public void newBatch(List<String> commands, String commandName) {
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
        commandName = commandName.trim().toLowerCase();
        this.batchesDao.saveNewBatch(new StoredCommandsBatch(commandName, commands));
    }    
        
    @Override
    public boolean deleteBatch(String commandName) {
        commandName = commandName.trim().toLowerCase();
        return this.batchesDao.removeBatch(commandName);
    }    
    
    @Override
    public List<StoredCommandsBatch> getAllBatches() {
        return this.batchesDao.getAllBatches();
    }    
    
    @Override
    public List<StoredCommandsBatch> getBatches(String commandName) {
        commandName = commandName.trim().toLowerCase();
        return this.batchesDao.getBatchesByName(commandName);
    }    
}
