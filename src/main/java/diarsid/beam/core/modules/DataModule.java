/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoKeyValueStorage;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoNamedEntities;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends StoppableBeamModule  {
    
    DaoNamedEntities getDaoNamedEntities();
    
    DaoLocations getDaoLocations();
    
    DaoBatches getDaoBatches();
    
    DaoCommands getDaoCommands();
    
    DaoKeyValueStorage getDaoKeyValueStorage();
}
