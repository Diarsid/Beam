/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.BeamModule;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoBatches;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoCommands;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoKeyValueStorage;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocationSubPathChoices;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocationSubPaths;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoLocations;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoNamedEntities;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPersistableCacheData;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPictures;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoTasks;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoWebDirectories;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoWebPages;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoNamedRectangles;

/**
 *
 * @author Diarsid
 */
public interface ResponsiveDataModule extends BeamModule {
    
    ResponsiveDaoNamedEntities namedEntities();
    
    ResponsiveDaoLocations locations();
    
    ResponsiveDaoBatches batches();
    
    ResponsiveDaoCommands commands();
    
    ResponsiveDaoPatternChoices patternChoices();
    
    ResponsiveDaoKeyValueStorage keyValues();
    
    ResponsiveDaoTasks tasks();
    
    ResponsiveDaoWebPages webPages();
    
    ResponsiveDaoPictures images();
    
    ResponsiveDaoWebDirectories webDirectories();
    
    ResponsiveDaoLocationSubPaths locationSubPaths();
    
    ResponsiveDaoLocationSubPathChoices locationSubPathChoices();
    
    ResponsiveDaoPersistableCacheData<Boolean> cachedSimilarity();
    
    ResponsiveDaoPersistableCacheData<Float> cachedWeight();
    
    ResponsiveDaoNamedRectangles windowPositions();
    
}
