/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.InterpreterHolderModule;
import diarsid.beam.core.modules.IoModule;

import com.drs.gem.injector.module.GemModuleBuilder;

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorkerBuilder implements GemModuleBuilder<DomainKeeperModule> {
    
    private final DataModule dataModule;
    private final IoModule ioModule;
    private final InterpreterHolderModule interpreterHolderModule;
    
    public DomainKeeperModuleWorkerBuilder(
            DataModule dataModule, 
            IoModule ioModule, 
            InterpreterHolderModule interpreterHolderModule) {
        this.dataModule = dataModule;
        this.ioModule = ioModule;
        this.interpreterHolderModule = interpreterHolderModule;
    }

    @Override
    public DomainKeeperModule buildModule() {
        InnerIoEngine ioEngine = this.ioModule.getInnerIoEngine();
        Interpreter interpreter = this.interpreterHolderModule.getInterpreter();
        KeeperDialogHelper dialogHelper = new KeeperDialogHelper(ioEngine);
        
        LocationsKeeper locationsKeeper;
        BatchesKeeper batchesKeeper;
        ProgramsKeeper programsKeeper;
        
        locationsKeeper = new LocationsKeeperWorker(
                dataModule.getDaoLocations(), 
                ioEngine, 
                dialogHelper);
        batchesKeeper = new BatchesKeeperWorker(
                dataModule.getDaoBatches(), 
                ioEngine, 
                dialogHelper, 
                interpreter);
        programsKeeper = new ProgramsKeeperWorker(
                ioEngine, 
                programsCatalog, 
                dialogHelper);
        
        return new DomainKeeperModuleWorker(
                locationsKeeper, 
                batchesKeeper, 
                programsKeeper);
    }
}
