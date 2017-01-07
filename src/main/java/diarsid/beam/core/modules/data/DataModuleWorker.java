/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import diarsid.beam.core.modules.DataModule;


class DataModuleWorker implements DataModule {
    
    private final DataBase dataBase;
    private final DaoLocations daoLocations;
    private final DaoBatches daoBatches;
    
    DataModuleWorker(DataBase dataBase, DaosProvider daosProvider) {
        this.dataBase = dataBase;
        this.daoLocations = daosProvider.createDaoLocations();
        this.daoBatches = daosProvider.createDaoBatches();
    }

    @Override
    public DaoLocations getDaoLocations() {
        return this.daoLocations;
    }

    @Override
    public DaoBatches getDaoBatches() {
        return this.daoBatches;
    }

    @Override
    public void stopModule() {
        this.dataBase.disconnect();
    }
}
