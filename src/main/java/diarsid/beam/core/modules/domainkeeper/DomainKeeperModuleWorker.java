/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.modules.DomainKeeperModule;

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorker implements DomainKeeperModule {
    
    private final LocationsKeeper locationsKeeper;
    private final BatchesKeeper batchesKeeper;
    private final ProgramsKeeper programsKeeper;
    private final TasksKeeper tasksKeeper;

    public DomainKeeperModuleWorker(
            LocationsKeeper locationsKeeper, 
            BatchesKeeper batchesKeeper, 
            ProgramsKeeper programsKeeper,
            TasksKeeper tasksKeeper) {
        this.locationsKeeper = locationsKeeper;
        this.batchesKeeper = batchesKeeper;
        this.programsKeeper = programsKeeper;
        this.tasksKeeper = tasksKeeper;
    }

    @Override
    public LocationsKeeper getLocationsKeeper() {
        return this.locationsKeeper;
    }

    @Override
    public BatchesKeeper getBatchesKeeper() {
        return this.batchesKeeper;
    }

    @Override
    public void stopModule() {
        // do nothing;
    }

    @Override
    public ProgramsKeeper getProgramsKeeper() {
        return this.programsKeeper;
    }

    @Override
    public TasksKeeper getTasksKeeper() {
        return this.tasksKeeper;
    }
}
