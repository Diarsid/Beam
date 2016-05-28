/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.processors.workers;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.processors.ProcessorLocations;
import diarsid.beam.core.modules.executor.processors.ProcessorNotes;
import diarsid.beam.core.modules.executor.processors.ProcessorPrograms;
import diarsid.beam.core.modules.executor.processors.ProcessorWebPages;
import diarsid.beam.core.modules.executor.processors.ProcessorsBuilder;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;
import diarsid.beam.core.modules.executor.processors.ProcessorCommandsBatches;

/**
 *
 * @author Diarsid
 */
public class ProcessorsBuilderImpl implements ProcessorsBuilder {
    
    private final IoInnerModule ioEngine;
    private final DataModule dataModule;
    private final ConfigModule configModule;
    private final IntelligentExecutorCommandContext intellContext;
    private final OS os;
    
    public ProcessorsBuilderImpl(
            IoInnerModule io, 
            DataModule data, 
            ConfigModule config,
            IntelligentExecutorCommandContext intell,
            OS os) {
        
        this.ioEngine = io;
        this.dataModule = data;
        this.intellContext = intell;
        this.configModule = config;
        this.os = os;
    }
    
    @Override
    public ProcessorWebPages buildProcessorWebPages() {
        return new ProcessorWebPagesWorker(
                this.ioEngine, 
                this.os, 
                this.dataModule.getWebPagesHandler(), 
                this.intellContext);
    }
    
    @Override
    public ProcessorCommandsBatches buildProcessorBatches() {
        return new ProcessorCommandsBatchesWorker(
                this.ioEngine, 
                this.dataModule.getCommandsDao(), 
                this.intellContext);
    }
    
    @Override
    public ProcessorNotes buildProcessorNotes() {
        Location notes = new Location(
                "notes", this.configModule.get(Config.NOTES_LOCATION));
        return new ProcessorNotesWorker(this.os, notes);
    }
    
    @Override
    public ProcessorLocations buildProcessorLocations() {
        return new ProcessorLocationsWorker(
                this.ioEngine, 
                this.os, 
                this.dataModule.getLocationsHandler(), 
                this.intellContext);
    }
    
    @Override
    public ProcessorPrograms buildProcessorPrograms() {
        return new ProcessorProgramsWorker(this.ioEngine, this.os);
    }
}
