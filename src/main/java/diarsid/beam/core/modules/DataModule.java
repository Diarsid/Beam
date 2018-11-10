/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.base.analyze.similarity.CachedSimilarity;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocationSubPathChoices;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DaoPatternChoices;
import diarsid.beam.core.modules.data.DaoPersistableCacheData;
import diarsid.beam.core.modules.data.DaoPictures;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends StoppableBeamModule  {
    
    DaoNamedEntities namedEntities();
    
    DaoLocations locations();
    
    DaoBatches batches();
    
    DaoCommands commands();
    
    DaoPatternChoices patternChoices();
    
    DaoKeyValueStorage keyValues();
    
    DaoTasks tasks();
    
    DaoWebPages webPages();
    
    DaoPictures images();
    
    DaoWebDirectories webDirectories();
    
    DaoLocationSubPaths locationSubPaths();
    
    DaoLocationSubPathChoices locationSubPathChoices();
    
    DaoPersistableCacheData<CachedSimilarity> cachedSimilarity();
    
    DaoPersistableCacheData<Float> cachedWeight();
    
}
