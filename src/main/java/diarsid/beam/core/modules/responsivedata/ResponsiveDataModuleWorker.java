/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoModule;
import diarsid.beam.core.modules.ResponsiveDataModule;

import static diarsid.beam.core.base.events.BeamEventRuntime.subscribeOnRequestsForPayloadOf;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDataModuleWorker implements ResponsiveDataModule {
    
    private final ResponsiveDaoLocations daoLocations;
    private final ResponsiveDaoBatches daoBatches;
    private final ResponsiveDaoNamedEntities daoNamedEntities;
    private final ResponsiveDaoCommands daoCommands;
    private final ResponsiveDaoPatternChoices daoPatternChoices;
    private final ResponsiveDaoKeyValueStorage daoKeyValueStorage;
    private final ResponsiveDaoTasks daoTasks;
    private final ResponsiveDaoWebPages daoWebPages;
    private final ResponsiveDaoWebDirectories daoWebDirectories;
    private final ResponsiveDaoPictures daoPictures;
    private final ResponsiveDaoLocationSubPaths daoLocationSubPaths;
    private final ResponsiveDaoLocationSubPathChoices daoLocationSubPathChoices;
    private final ResponsiveDaoPersistableCacheData<Boolean> daoSimilarityCache;
    private final ResponsiveDaoPersistableCacheData<Float> daoWeightCache;
    private final ResponsiveDaoNamedRectangles persistablePositions;

    ResponsiveDataModuleWorker(IoModule ioModule, DataModule dataModule) {
        InnerIoEngine ioEngine = ioModule.getInnerIoEngine();
        
        this.daoBatches = 
                new ResponsiveDaoBatches(dataModule.batches(), ioEngine);
        this.daoLocations = 
                new ResponsiveDaoLocations(dataModule.locations(), ioEngine);
        this.daoPictures = 
                new ResponsiveDaoPictures(dataModule.images(), ioEngine);
        this.daoNamedEntities = 
                new ResponsiveDaoNamedEntities(dataModule.namedEntities(), ioEngine);
        this.daoCommands = 
                new ResponsiveDaoCommands(dataModule.commands(), ioEngine);
        this.daoPatternChoices = 
                new ResponsiveDaoPatternChoices(dataModule.patternChoices(), ioEngine);
        this.daoKeyValueStorage = 
                new ResponsiveDaoKeyValueStorage(dataModule.keyValues(), ioEngine);
        this.daoTasks = 
                new ResponsiveDaoTasks(dataModule.tasks(), ioEngine);
        this.daoWebPages = 
                new ResponsiveDaoWebPages(dataModule.webPages(), ioEngine);
        this.daoWebDirectories = 
                new ResponsiveDaoWebDirectories(dataModule.webDirectories(), ioEngine);
        this.daoLocationSubPaths = 
                new ResponsiveDaoLocationSubPaths(dataModule.locationSubPaths(), ioEngine);
        this.daoLocationSubPathChoices = 
                new ResponsiveDaoLocationSubPathChoices(dataModule.locationSubPathChoices(), ioEngine);
        this.daoSimilarityCache = 
                new ResponsiveDaoPersistableCacheData<>(dataModule.cachedSimilarity(), ioEngine);
        this.daoWeightCache = 
                new ResponsiveDaoPersistableCacheData<>(dataModule.cachedWeight(), ioEngine);
        this.persistablePositions = 
                new ResponsiveDaoNamedRectangles(dataModule.namedRectangles(), ioEngine);
        
        subscribeOnRequestsForPayloadOf(ResponsiveDataModule.class, () -> this);
    }

    @Override
    public ResponsiveDaoNamedEntities namedEntities() {
        return this.daoNamedEntities;
    }

    @Override
    public ResponsiveDaoLocations locations() {
        return this.daoLocations;
    }

    @Override
    public ResponsiveDaoBatches batches() {
        return this.daoBatches;
    }

    @Override
    public ResponsiveDaoCommands commands() {
        return this.daoCommands;
    }

    @Override
    public ResponsiveDaoPatternChoices patternChoices() {
        return this.daoPatternChoices;
    }

    @Override
    public ResponsiveDaoKeyValueStorage keyValues() {
        return this.daoKeyValueStorage;
    }

    @Override
    public ResponsiveDaoTasks tasks() {
        return this.daoTasks;
    }

    @Override
    public ResponsiveDaoWebPages webPages() {
        return this.daoWebPages;
    }

    @Override
    public ResponsiveDaoPictures images() {
        return this.daoPictures;
    }

    @Override
    public ResponsiveDaoWebDirectories webDirectories() {
        return this.daoWebDirectories;
    }

    @Override
    public ResponsiveDaoLocationSubPaths locationSubPaths() {
        return this.daoLocationSubPaths;
    }

    @Override
    public ResponsiveDaoLocationSubPathChoices locationSubPathChoices() {
        return this.daoLocationSubPathChoices;
    }

    @Override
    public ResponsiveDaoPersistableCacheData<Boolean> cachedSimilarity() {
        return this.daoSimilarityCache;
    }

    @Override
    public ResponsiveDaoPersistableCacheData<Float> cachedWeight() {
        return this.daoWeightCache;
    }

    @Override
    public ResponsiveDaoNamedRectangles windowPositions() {
        return this.persistablePositions;
    }
    
    
}
