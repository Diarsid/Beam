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
    private final DaoCommandsChoices daoCommandsChoices;
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
        this.daoCommandsChoices = daosProvider.createDaoCommandsChoices();
        this.daoKeyValueStorage = daosProvider.createDaoKeyValueStorage();
        this.daoTasks = daosProvider.createDaoTasks();
        this.daoWebPages = daosProvider.createDaoWebPages();
        this.daoWebDirectories = daosProvider.createDaoWebDirectories();
    }

    @Override
    public DaoLocations locations() {
        return this.daoLocations;
    }

    @Override
    public DaoBatches batches() {
        return this.daoBatches;
    }

    @Override
    public void stopModule() {
        this.dataBase.disconnect();
    }

    @Override
    public DaoNamedEntities namedEntities() {
        return this.daoNamedEntities;
    }

    @Override
    public DaoCommands commands() {
        return this.daoCommands;
    }
    
    @Override
    public DaoCommandsChoices commandsChoices() {
        return this.daoCommandsChoices;
    }

    @Override
    public DaoKeyValueStorage keyValues() {
        return this.daoKeyValueStorage;
    }

    @Override
    public DaoTasks tasks() {
        return this.daoTasks;
    }

    @Override
    public DaoWebPages webPages() {
        return this.daoWebPages;
    }

    @Override
    public DaoWebDirectories webDirectories() {
        return this.daoWebDirectories;
    }
}
