/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.Set;

import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.modules.DomainKeeperModule;

import static diarsid.beam.core.base.util.CollectionsUtils.toSet;

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorker implements DomainKeeperModule {
    
    private final LocationsKeeper locationsKeeper;
    private final BatchesKeeper batchesKeeper;
    private final ProgramsKeeper programsKeeper;
    private final TasksKeeper tasksKeeper;
    private final WebPagesKeeper webPagesKeeper;
    private final WebDirectoriesKeeper webDirectoriesKeeper;
    private final CommandsMemoryKeeper commandsMemoryKeeper;
    private final NamedEntitiesKeeper defaultNamedEntitiesKeeper;
    private final Set<NamedEntitiesKeeper> allDedicatedNamedEntitiesKeepers;

    public DomainKeeperModuleWorker(
            LocationsKeeper locationsKeeper, 
            BatchesKeeper batchesKeeper, 
            ProgramsKeeper programsKeeper,
            TasksKeeper tasksKeeper,
            WebPagesKeeper webPagesKeeper,
            WebDirectoriesKeeper webDirectoriesKeeper,
            CommandsMemoryKeeper commandsMemoryKeeper,
            NamedEntitiesKeeper namedEntitiesKeeper) {
        this.locationsKeeper = locationsKeeper;
        this.batchesKeeper = batchesKeeper;
        this.programsKeeper = programsKeeper;
        this.tasksKeeper = tasksKeeper;
        this.webPagesKeeper = webPagesKeeper;
        this.webDirectoriesKeeper = webDirectoriesKeeper;
        this.commandsMemoryKeeper = commandsMemoryKeeper;
        this.defaultNamedEntitiesKeeper = namedEntitiesKeeper;
        this.allDedicatedNamedEntitiesKeepers = toSet(
                locationsKeeper, 
                batchesKeeper, 
                programsKeeper, 
                webPagesKeeper);
    }

    @Override
    public LocationsKeeper locations() {
        return this.locationsKeeper;
    }

    @Override
    public BatchesKeeper batches() {
        return this.batchesKeeper;
    }

    @Override
    public void stopModule() {
        // do nothing;
    }

    @Override
    public ProgramsKeeper programs() {
        return this.programsKeeper;
    }

    @Override
    public TasksKeeper tasks() {
        return this.tasksKeeper;
    }
    
    @Override
    public WebPagesKeeper webPages() {
        return this.webPagesKeeper;
    }

    @Override
    public WebDirectoriesKeeper webDirectories() {
        return this.webDirectoriesKeeper;
    }

    @Override
    public CommandsMemoryKeeper commandsMemory() {
        return this.commandsMemoryKeeper;
    }

    @Override
    public NamedEntitiesKeeper entitiesOperatedBy(InvocationEntityCommand command) {
        return this.allDedicatedNamedEntitiesKeepers
                .stream()
                .filter(keeper -> keeper.isSubjectedTo(command))
                .findFirst()
                .orElse(this.defaultNamedEntitiesKeeper);
    }
}
