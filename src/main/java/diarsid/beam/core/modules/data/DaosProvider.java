/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

/**
 *
 * @author Diarsid
 */
public interface DaosProvider {
    
    DaoLocations createDaoLocations();
    
    DaoBatches createDaoBatches();
    
    DaoNamedEntities createDaoNamedEntities();
    
    DaoCommands createDaoCommands();
    
    DaoPatternChoices createDaoPatternChoices();
    
    DaoKeyValueStorage createDaoKeyValueStorage();
    
    DaoTasks createDaoTasks();
    
    DaoWebDirectories createDaoWebDirectories();
    
    DaoWebPages createDaoWebPages();

    DaoPictures createDaoImages();
    
    DaoLocationSubPaths createDaoLocationSubPaths();
    
    DaoLocationSubPathChoices createDaoLocationSubPathChoices();
    
    DaoPersistableCacheData<Boolean> createDaoSimilarityCache();
    
    DaoPersistableCacheData<Float> createDaoWeightCache();
    
}
