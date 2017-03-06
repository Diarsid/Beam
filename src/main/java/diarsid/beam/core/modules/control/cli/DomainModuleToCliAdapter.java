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
    
    public DomainModuleToCliAdapter(
            DomainKeeperModule domainModule, InnerIoEngine ioEngine) {
        this.locationsKeeperAdapter = 
                new CliAdapterForLocationsKeeper(
                        domainModule.getLocationsKeeper(), ioEngine);
        this.batchesKeeperAdapter = 
                new CliAdapterForBatchesKeeper(
                        domainModule.getBatchesKeeper(), ioEngine);
        this.programsKeeperAdapter = 
                new CliAdapterForProgramsKeeper(
                        domainModule.getProgramsKeeper(), ioEngine);
        this.tasksKeeperAdapter = 
                new CliAdapterForTasksKeeper(
                        domainModule.getTasksKeeper(), ioEngine);
        this.webDirectoriesKeeperAdapter = 
                new CliAdapterForWebDirectoriesKeeper(
                        domainModule.getWebDirectoriesKeeper(), ioEngine);
        this.webPagesKeeperAdapter = 
                new CliAdapterForWebPagesKeeper(
                        domainModule.getWebPagesKeeper(), ioEngine);
        
    }
    
    CliAdapterForLocationsKeeper getLocationsAdapter() {
        return this.locationsKeeperAdapter;
    }
    
    CliAdapterForBatchesKeeper getBatchesAdapter() {
        return this.batchesKeeperAdapter;
    }
    
    CliAdapterForProgramsKeeper getProgramsAdapter() {
        return this.programsKeeperAdapter;
    }
    
    CliAdapterForTasksKeeper getTasksAdapter() {
        return this.tasksKeeperAdapter;
    }
    
    CliAdapterForWebDirectoriesKeeper getWebDirectoriesAdapter() {
        return this.webDirectoriesKeeperAdapter;
    }
    
    CliAdapterForWebPagesKeeper getWebPagesAdapter() {
        return this.webPagesKeeperAdapter;
    }
}
