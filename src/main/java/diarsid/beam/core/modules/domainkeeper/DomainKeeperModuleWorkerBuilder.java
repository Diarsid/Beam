/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.base.os.treewalking.advanced.Walker;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.TimeParser;
import diarsid.beam.core.domain.inputparsing.webpages.WebObjectsInputParser;
import diarsid.beam.core.modules.BeamEnvironmentModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.ResponsiveDataModule;
import diarsid.support.objects.Pool;
import diarsid.support.objects.Pools;

import com.drs.gem.injector.module.GemModuleBuilder;

import static diarsid.beam.core.Beam.systemInitiator;
import static diarsid.beam.core.base.os.treewalking.advanced.Walker.newWalker;
import static diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector.getFolderTypeDetector;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.allowedTimePeriodsParser;
import static diarsid.beam.core.domain.inputparsing.time.TimeParsing.timePatternParsersHolder;
import static diarsid.support.objects.Pools.pools;


/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorkerBuilder implements GemModuleBuilder<DomainKeeperModule> {
    
    private final ResponsiveDataModule responsiveDataModule;
    private final IoModule ioModule;
    private final BeamEnvironmentModule beamEnvironmentModule;
    
    public DomainKeeperModuleWorkerBuilder(
            ResponsiveDataModule dataModule, 
            IoModule ioModule, 
            BeamEnvironmentModule beamEnvironmentModule) {
        this.responsiveDataModule = dataModule;
        this.ioModule = ioModule;
        this.beamEnvironmentModule = beamEnvironmentModule;
    }

    @Override
    public DomainKeeperModule buildModule() {
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        Interpreter interpreter = this.beamEnvironmentModule.interpreter();
        Initiator systemInitiator = systemInitiator();
        KeeperDialogHelper dialogHelper = new KeeperDialogHelper(ioEngine);
        
        LocationsInputParser locationsInputParser;
        PropertyAndTextParser propertyAndTextParser;
        TimeParser timeParser;
        AllowedTimePeriodsParser timePeriodsParser;
        WebObjectsInputParser webObjectsParser;
        
        locationsInputParser = new LocationsInputParser();
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
        LocationSubPathKeeper locationSubPathKeeper;
        NamedEntitiesKeeper defaultKeeper;
        AllKeeper allKeeper;
        
        Pools pools = pools();
        
        Pool<KeeperLoopValidationDialog> dialogPool = pools.createPool(
                KeeperLoopValidationDialog.class, 
                () -> new KeeperLoopValidationDialog());
        
        Walker walker = newWalker(
                ioEngine, 
                this.responsiveDataModule.patternChoices(), 
                getFolderTypeDetector(),
                this.beamEnvironmentModule.analyze(),
                this.beamEnvironmentModule.similarity(),
                pools);
        
        commandsMemoryKeeper = new CommandsMemoryKeeperWorker(
                this.responsiveDataModule.commands(), 
                this.responsiveDataModule.patternChoices(),
                ioEngine,
                this.beamEnvironmentModule.analyze(),
                dialogPool);
        locationsKeeper = new LocationsKeeperWorker(
                this.responsiveDataModule.locations(),
                this.responsiveDataModule.locationSubPaths(),
                this.responsiveDataModule.patternChoices(),
                commandsMemoryKeeper,
                this.beamEnvironmentModule.analyze(), 
                dialogPool,
                ioEngine, 
                walker,
                locationsInputParser, 
                propertyAndTextParser);
        batchesKeeper = new BatchesKeeperWorker(
                this.responsiveDataModule.batches(), 
                this.responsiveDataModule.patternChoices(),
                commandsMemoryKeeper,
                this.beamEnvironmentModule.analyze(), 
                dialogPool,
                ioEngine, 
                dialogHelper, 
                interpreter, 
                propertyAndTextParser);
        programsKeeper = new ProgramsKeeperWorker(
                ioEngine,
                walker,
                this.beamEnvironmentModule.analyze(),
                dialogPool,
                this.beamEnvironmentModule.programsCatalog());
        tasksKeeper = new TasksKeeperWorker(
                ioEngine, 
                this.responsiveDataModule.tasks(), 
                dialogHelper, 
                timeParser, 
                timePeriodsParser);
        pagesKeeper = new WebPagesKeeperWorker(
                this.responsiveDataModule.webPages(), 
                this.responsiveDataModule.webDirectories(), 
                this.responsiveDataModule.images(),
                commandsMemoryKeeper,
                this.responsiveDataModule.patternChoices(),
                ioEngine, 
                this.beamEnvironmentModule.analyze(), 
                dialogPool,
                this.ioModule.gui(),
                systemInitiator,
                dialogHelper, 
                propertyAndTextParser, 
                webObjectsParser);
        directoriesKeeper = new WebDirectoriesKeeperWorker(
                this.responsiveDataModule.webDirectories(), 
                commandsMemoryKeeper,
                ioEngine, 
                systemInitiator,
                dialogPool,
                dialogHelper, 
                webObjectsParser);   
        defaultKeeper = new NamedEntitiesKeeperWorker(
                ioEngine, 
                this.responsiveDataModule.namedEntities(),
                this.beamEnvironmentModule.analyze());
        locationSubPathKeeper = new LocationSubPathKeeperWorker(
                this.responsiveDataModule.locationSubPaths(), 
                this.responsiveDataModule.locationSubPathChoices(),
                this.beamEnvironmentModule.analyze(),
                ioEngine);
        notesKeeper = new NotesKeeperWorker(
                ioEngine,
                this.beamEnvironmentModule.notesCatalog(), 
                this.beamEnvironmentModule.analyze(), 
                dialogPool,
                dialogHelper);
        allKeeper = new AllKeeperWorker(
                this.responsiveDataModule,
                programsKeeper, 
                this.beamEnvironmentModule.analyze());
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
                locationSubPathKeeper,
                allKeeper);
    }
}
