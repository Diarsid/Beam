/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoCommandsChoices;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaoWebDirectories;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.modules.data.DaoPictures;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends StoppableBeamModule  {
    
    DaoNamedEntities namedEntities();
    
    DaoLocations locations();
    
    DaoBatches batches();
    
    DaoCommands commands();
    
    DaoCommandsChoices commandsChoices();
    
    DaoKeyValueStorage keyValues();
    
    DaoTasks tasks();
    
    DaoWebPages webPages();
    
    DaoPictures images();
    
    DaoWebDirectories webDirectories();
}
