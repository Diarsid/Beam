/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain;

import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.DomainKeeperModule;
import diarsid.beam.core.modules.domain.keepers.LocationsKeeperWorker;

/**
 *
 * @author Diarsid
 */
public class DomainKeeperModuleWorker implements DomainKeeperModule {
    
    private final LocationsKeeper locationsKeeper;
    
    public DomainKeeperModuleWorker(
            DataModule dataModule, InnerIoEngine ioEngine) {
        CommandConsistencyChecker consistencyChecker = new CommandConsistencyChecker(ioEngine);
        this.locationsKeeper = new LocationsKeeperWorker(
                dataModule.getDaoLocations(), ioEngine, consistencyChecker);
    }

    @Override
    public LocationsKeeper getLocationsKeeper() {
        return this.locationsKeeper;
    }

    @Override
    public void stopModule() {
        // do nothing;
    }
}
