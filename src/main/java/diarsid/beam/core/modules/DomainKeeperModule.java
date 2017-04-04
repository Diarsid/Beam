/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.modules.domainkeeper.BatchesKeeper;
import diarsid.beam.core.modules.domainkeeper.CommandsMemoryKeeper;
import diarsid.beam.core.modules.domainkeeper.LocationsKeeper;
import diarsid.beam.core.modules.domainkeeper.NamedEntitiesKeeper;
import diarsid.beam.core.modules.domainkeeper.ProgramsKeeper;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;

/**
 *
 * @author Diarsid
 */
public interface DomainKeeperModule extends StoppableBeamModule  {
    
    NamedEntitiesKeeper entitiesOperatedBy(InvocationEntityCommand command);
    
    LocationsKeeper locations();
    
    BatchesKeeper batches();
    
    ProgramsKeeper programs();
    
    TasksKeeper tasks();
    
    WebPagesKeeper webPages();
    
    WebDirectoriesKeeper webDirectories();
    
    CommandsMemoryKeeper commandsMemory();
}
