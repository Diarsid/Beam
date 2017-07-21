/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.TimeAndTextParser;
import diarsid.beam.core.domain.inputparsing.time.TimePatternParsersHolder;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.allowedTimePeriodsParser;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.timeAndTextParser;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.timePatternParsersHolder;

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorkerBuilder implements GemModuleBuilder<DomainKeeperModule> {
    
    private final DataModule dataModule;
    private final IoModule ioModule;
    private final ApplicationComponentsHolderModule appComponentsHolderModule;
    
    public DomainKeeperModuleWorkerBuilder(
            DataModule dataModule, 
            IoModule ioModule, 
            ApplicationComponentsHolderModule appComponentsHolderModule) {
        this.dataModule = dataModule;
        this.ioModule = ioModule;
        this.appComponentsHolderModule = appComponentsHolderModule;
    }

    @Override
    public DomainKeeperModule buildModule() {
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        Interpreter interpreter = this.appComponentsHolderModule.getInterpreter();
        Initiator systemInitiator = systemInitiator();
        KeeperDialogHelper dialogHelper = new KeeperDialogHelper(ioEngine);
        
        LocationsInputParser locationsInputParser;
        TimeAndTextParser timeAndTextParser;
        PropertyAndTextParser propertyAndTextParser;
        TimePatternParsersHolder timeParser;
        AllowedTimePeriodsParser timePeriodsParser;
        WebObjectsInputParser webObjectsParser;
        
        locationsInputParser = new LocationsInputParser();
        timeAndTextParser = timeAndTextParser();
        timeParser = timePatternParsersHolder();
        propertyAndTextParser = new PropertyAndTextParser();
        timePeriodsParser = allowedTimePeriodsParser();
        webObjectsParser = new WebObjectsInputParser();
        
        LocationsKeeper locationsKeeper;
        BatchesKeeper batchesKeeper;
        ProgramsKeeper programsKeeper;
        TasksKeeper tasksKeeper;
        WebPagesKeeper pagesKeeper;
        WebDirectoriesKeeper directoriesKeeper;
        NotesKeeper notesKeeper;
        CommandsMemoryKeeper commandsMemoryKeeper;
        NamedEntitiesKeeper defaultKeeper;
        AllKeeper allKeeper;
        
        commandsMemoryKeeper = new CommandsMemoryKeeperWorker(
                this.dataModule.commands(), 
                this.dataModule.commandsChoices(),
                ioEngine, 
                dialogHelper);
        locationsKeeper = new LocationsKeeperWorker(
                this.dataModule.locations(), 
                commandsMemoryKeeper,
                ioEngine, 
                dialogHelper, 
                locationsInputParser, 
                propertyAndTextParser);
        batchesKeeper = new BatchesKeeperWorker(
                this.dataModule.batches(), 
                commandsMemoryKeeper,
                ioEngine, 
                dialogHelper, 
                interpreter, 
                propertyAndTextParser);
        programsKeeper = new ProgramsKeeperWorker(
                ioEngine,
                this.appComponentsHolderModule.getProgramsCatalog(), 
                dialogHelper);
        tasksKeeper = new TasksKeeperWorker(
                ioEngine, 
                this.dataModule.tasks(), 
                dialogHelper, 
                timeAndTextParser, 
                timeParser, 
                timePeriodsParser);
        pagesKeeper = new WebPagesKeeperWorker(
                this.dataModule.webPages(), 
                this.dataModule.webDirectories(), 
                commandsMemoryKeeper,
                ioEngine, 
                systemInitiator,
                dialogHelper, 
                propertyAndTextParser, 
                webObjectsParser);
        directoriesKeeper = new WebDirectoriesKeeperWorker(
                this.dataModule.webDirectories(), 
                commandsMemoryKeeper,
                ioEngine, 
                systemInitiator,
                dialogHelper, 
                webObjectsParser);   
        defaultKeeper = new NamedEntitiesKeeperWorker(
                ioEngine, 
                this.dataModule.namedEntities());
        notesKeeper = new NotesKeeperWorker(
                ioEngine,
                this.appComponentsHolderModule.getNotesCatalog(), 
                dialogHelper);
        allKeeper = new AllKeeperWorker(
                this.dataModule,
                programsKeeper);
        return new DomainKeeperModuleWorker(
                locationsKeeper, 
                batchesKeeper, 
                programsKeeper, 
                tasksKeeper, 
                pagesKeeper, 
                directoriesKeeper, 
                notesKeeper,
                commandsMemoryKeeper, 
                defaultKeeper, 
                allKeeper);
    }
}
