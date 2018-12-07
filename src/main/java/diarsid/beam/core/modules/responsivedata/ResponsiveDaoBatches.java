/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.modules.data.DaoBatches;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoBatches extends BeamCommonResponsiveDao<DaoBatches> {

    ResponsiveDaoBatches(DaoBatches daoBatches, InnerIoEngine ioEngine) {
        super(daoBatches, ioEngine);
    }
    
    public boolean isNameFree(Initiator initiator, String exactName) { 
        try {
            return super.dao().isNameFree(exactName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public List<String> getBatchNamesByNamePattern(
            Initiator initiator, String batchName) { 
        try {
            return super.dao().getBatchNamesByNamePattern(batchName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public Optional<Batch> getBatchByExactName(Initiator initiator, String name) { 
        try {
            return super.dao().getBatchByExactName(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public boolean saveBatch(
            Initiator initiator, Batch batch) { 
        try {
            return super.dao().saveBatch(batch);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean removeBatch(
            Initiator initiator, String batchName) { 
        try {
            return super.dao().removeBatch(batchName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editBatchName(
            Initiator initiator, String batchName, String newName) { 
        try {
            return super.dao().editBatchName(batchName, newName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editBatchCommands(
            Initiator initiator, String batchName, List<ExecutorCommand> newCommands) { 
        try {
            return super.dao().editBatchCommands(batchName, newCommands);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editBatchOneCommand(
            Initiator initiator, String batchName, int commandOrder, ExecutorCommand newCommand) { 
        try {
            return super.dao().editBatchOneCommand(batchName, commandOrder, newCommand);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public List<Batch> getAllBatches(
            Initiator initiator) { 
        try {
            return super.dao().getAllBatches();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
}
