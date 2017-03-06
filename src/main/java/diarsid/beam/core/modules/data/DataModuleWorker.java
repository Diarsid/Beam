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
    private final DaoNamedEntities daoNamedEntities;
    private final DaoCommands daoCommands;
    private final DaoKeyValueStorage daoKeyValueStorage;
    private final DaoTasks daoTasks;
    private final DaoWebPages daoWebPages;
    private final DaoWebDirectories daoWebDirectories;
    
    DataModuleWorker(DataBase dataBase, DaosProvider daosProvider) {
        this.dataBase = dataBase;
        this.daoLocations = daosProvider.createDaoLocations();
        this.daoBatches = daosProvider.createDaoBatches();
        this.daoNamedEntities = daosProvider.createDaoNamedEntities();
        this.daoCommands = daosProvider.createDaoCommands();
        this.daoKeyValueStorage = daosProvider.createDaoKeyValueStorage();
        this.daoTasks = daosProvider.createDaoTasks();
        this.daoWebPages = daosProvider.createDaoWebPages();
        this.daoWebDirectories = daosProvider.createDaoWebDirectories();
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

    @Override
    public DaoNamedEntities getDaoNamedEntities() {
        return this.daoNamedEntities;
    }

    @Override
    public DaoCommands getDaoCommands() {
        return this.daoCommands;
    }

    @Override
    public DaoKeyValueStorage getDaoKeyValueStorage() {
        return this.daoKeyValueStorage;
    }

    @Override
    public DaoTasks getDaoTasks() {
        return this.daoTasks;
    }

    @Override
    public DaoWebPages getDaoWebPages() {
        return this.daoWebPages;
    }

    @Override
    public DaoWebDirectories getDaoWebDirectories() {
        return this.daoWebDirectories;
    }
}
