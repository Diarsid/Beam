/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.modules.DomainKeeperModule;

/**
 *
 * @author Diarsid
 */
public class DomainModuleToCliAdapter {
    
    private final CliAdapterForLocationsKeeper locationsKeeperAdapter;
    private final CliAdapterForBatchesKeeper batchesKeeperAdapter;
    private final CliAdapterForProgramsKeeper programsKeeperAdapter;
    private final CliAdapterForTasksKeeper tasksKeeperAdapter;
    private final CliAdapterForWebDirectoriesKeeper webDirectoriesKeeperAdapter;
    private final CliAdapterForWebPagesKeeper webPagesKeeperAdapter;
    private final CliAdapterForNotesKeeper notesKeeperAdapter;
    
    public DomainModuleToCliAdapter(
            DomainKeeperModule domainModule, InnerIoEngine ioEngine) {
        this.locationsKeeperAdapter = 
                new CliAdapterForLocationsKeeper(
                        domainModule.locations(), ioEngine);
        this.batchesKeeperAdapter = 
                new CliAdapterForBatchesKeeper(
                        domainModule.batches(), ioEngine);
        this.programsKeeperAdapter = 
                new CliAdapterForProgramsKeeper(
                        domainModule.programs(), ioEngine);
        this.tasksKeeperAdapter = 
                new CliAdapterForTasksKeeper(
                        domainModule.tasks(), ioEngine);
        this.webDirectoriesKeeperAdapter = 
                new CliAdapterForWebDirectoriesKeeper(
                        domainModule.webDirectories(), ioEngine);
        this.webPagesKeeperAdapter = 
                new CliAdapterForWebPagesKeeper(
                        domainModule.webPages(), ioEngine);
        this.notesKeeperAdapter = 
                new CliAdapterForNotesKeeper(
                        domainModule.notes(), ioEngine);
    }
    
    CliAdapterForLocationsKeeper locationsAdapter() {
        return this.locationsKeeperAdapter;
    }
    
    CliAdapterForBatchesKeeper batchesAdapter() {
        return this.batchesKeeperAdapter;
    }
    
    CliAdapterForProgramsKeeper programsAdapter() {
        return this.programsKeeperAdapter;
    }
    
    CliAdapterForTasksKeeper tasksAdapter() {
        return this.tasksKeeperAdapter;
    }
    
    CliAdapterForWebDirectoriesKeeper webDirectoriesAdapter() {
        return this.webDirectoriesKeeperAdapter;
    }
    
    CliAdapterForWebPagesKeeper webPagesAdapter() {
        return this.webPagesKeeperAdapter;
    }
    
    CliAdapterForNotesKeeper notesAdaper() {
        return this.notesKeeperAdapter;
    }
}
