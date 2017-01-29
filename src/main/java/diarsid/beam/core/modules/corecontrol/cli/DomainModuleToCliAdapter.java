/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.cli;

import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.modules.DomainKeeperModule;

/**
 *
 * @author Diarsid
 */
public class DomainModuleToCliAdapter {
    
    private final CliAdapterForLocationsKeeper locationsKeeperAdapter;
    private final CliAdapterForBatchesKeeper batchesKeeperAdapter;
    private final CliAdapterForProgramsKeeper programsKeeperAdapter;
    
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
}
