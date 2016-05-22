/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import diarsid.beam.core.entities.Location;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

/**
 *
 * @author Diarsid
 */
class ProcessorsBuilder {
    
    private final IoInnerModule ioEngine;
    private final DataModule dataModule;
    private final ConfigModule configModule;
    private final IntelligentExecutorCommandContext intellContext;
    private final OS os;
    
    ProcessorsBuilder(
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
    
    ProcessorWebPages buildProcessorWebPages() {
        return new ProcessorWebPages(
                this.ioEngine, 
                this.os, 
                this.dataModule.getWebPagesHandler(), 
                intellContext);
    }
    
    ProcessorCommands buildProcessorCommands() {
        return new ProcessorCommands(
                this.ioEngine, 
                this.dataModule.getCommandsDao(), 
                this.intellContext);
    }
    
    ProcessorNotes buildProcessorNotes() {
        Location notes = new Location(
                "notes", 
                this.configModule.get(Config.NOTES_LOCATION));
        return new ProcessorNotes(this.os, notes);
    }
    
    ProcessorLocations buildProcessorLocations() {
        return new ProcessorLocations(
                this.ioEngine, 
                this.os, 
                this.dataModule.getLocationsHandler(), 
                this.intellContext);
    }
    
    ProcessorPrograms buildProcessorPrograms() {
        return new ProcessorPrograms(this.ioEngine, this.os);
    }
}
