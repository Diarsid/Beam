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

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorker implements DomainKeeperModule {
    
    private final LocationsKeeper locationsKeeper;
    private final BatchesKeeper batchesKeeper;
    
    public DomainKeeperModuleWorker(
            DataModule dataModule, 
            IoModule ioModule, 
            InterpreterHolderModule interpreterHolderModule) {
        InnerIoEngine ioEngine = ioModule.getInnerIoEngine();
        Interpreter interpreter = interpreterHolderModule.getInterpreter();
        KeeperDialogHelper dialogHelper = new KeeperDialogHelper(ioEngine);
        this.locationsKeeper = new LocationsKeeperWorker(
                dataModule.getDaoLocations(), 
                ioEngine, 
                dialogHelper);
        this.batchesKeeper = new BatchesKeeperWorker(
                dataModule.getDaoBatches(), 
                ioEngine, 
                dialogHelper, 
                interpreter);
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
}
