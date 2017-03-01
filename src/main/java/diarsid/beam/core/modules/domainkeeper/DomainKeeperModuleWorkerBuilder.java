/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.domain.inputparsing.common.PropertyAndTextParser;
import diarsid.beam.core.domain.inputparsing.locations.LocationsInputParser;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.TimeAndTextParser;
import diarsid.beam.core.domain.inputparsing.time.TimePatternParsersHolder;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

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
        KeeperDialogHelper dialogHelper = new KeeperDialogHelper(ioEngine);
        
        LocationsInputParser locationsInputParser;
        TimeAndTextParser timeAndTextParser;
        PropertyAndTextParser propertyAndTextParser;
        TimePatternParsersHolder timeParser;
        AllowedTimePeriodsParser timePeriodsParser;
        
        locationsInputParser = new LocationsInputParser();
        timeAndTextParser = timeAndTextParser();
        timeParser = timePatternParsersHolder();
        propertyAndTextParser = new PropertyAndTextParser();
        timePeriodsParser = allowedTimePeriodsParser();
        
        LocationsKeeper locationsKeeper;
        BatchesKeeper batchesKeeper;
        ProgramsKeeper programsKeeper;
        TasksKeeper tasksKeeper;
        WebPagesKeeper pagesKeeper;
        
        locationsKeeper = new LocationsKeeperWorker(
                this.dataModule.getDaoLocations(), 
                ioEngine, 
                dialogHelper, 
                locationsInputParser, 
                propertyAndTextParser);
        batchesKeeper = new BatchesKeeperWorker(
                this.dataModule.getDaoBatches(), 
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
                this.dataModule.getDaoTasks(), 
                dialogHelper, 
                timeAndTextParser, 
                timeParser, 
                timePeriodsParser);
//        pagesKeeper = new WebPagesKeeperWorker(
//                this.dataModule, 
//                daoDirectories, 
//                ioEngine, dialogHelper, propetyTextParser, parser);
        
        return new DomainKeeperModuleWorker(
                locationsKeeper, 
                batchesKeeper, 
                programsKeeper, 
                tasksKeeper);
    }
}
