/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.DataModule;

import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.events.BeamEventRuntime.awaitForPayloadRequestThenSupply;


class DataModuleWorker implements DataModule {
    
    private final DataBase dataBase;
    private final DaoLocations daoLocations;
    private final DaoBatches daoBatches;
    private final DaoNamedEntities daoNamedEntities;
    private final DaoCommands daoCommands;
    private final DaoPatternChoices daoPatternChoices;
    private final DaoKeyValueStorage daoKeyValueStorage;
    private final DaoTasks daoTasks;
    private final DaoWebPages daoWebPages;
    private final DaoWebDirectories daoWebDirectories;
    private final DaoPictures daoImages;
    private final DaoLocationSubPaths daoLocationSubPaths;
    private final DaoLocationSubPathChoices daoLocationSubPathChoices;
    
    DataModuleWorker(DataBase dataBase, DaosProvider daosProvider) {
        this.dataBase = dataBase;
        this.daoLocations = daosProvider.createDaoLocations();
        this.daoBatches = daosProvider.createDaoBatches();
        this.daoNamedEntities = daosProvider.createDaoNamedEntities();
        this.daoCommands = daosProvider.createDaoCommands();
        this.daoPatternChoices = daosProvider.createDaoPatternChoices();
        this.daoKeyValueStorage = daosProvider.createDaoKeyValueStorage();
        this.daoTasks = daosProvider.createDaoTasks();
        this.daoWebPages = daosProvider.createDaoWebPages();
        this.daoWebDirectories = daosProvider.createDaoWebDirectories();
        this.daoImages = daosProvider.createDaoImages();
        this.daoLocationSubPaths = daosProvider.createDaoLocationSubPaths();
        this.daoLocationSubPathChoices = daosProvider.createDaoLocationSubPathChoices();
        DaoSimilarityCache daoSimilarityCache = daosProvider.createDaoSimilarityCache();
        asyncDo(() -> {
            awaitForPayloadRequestThenSupply(daoSimilarityCache, DaoSimilarityCache.class);
        });        
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
    public DaoPatternChoices patternChoices() {
        return this.daoPatternChoices;
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
    public DaoPictures images() {
        return this.daoImages;
    }

    @Override
    public DaoWebDirectories webDirectories() {
        return this.daoWebDirectories;
    }
    
    @Override
    public DaoLocationSubPaths locationSubPaths() {
        return this.daoLocationSubPaths;
    }    
    
    @Override
    public DaoLocationSubPathChoices locationSubPathChoices() {
        return this.daoLocationSubPathChoices;
    }
}
