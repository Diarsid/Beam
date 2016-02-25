/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoIntellChoice;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.DaoWebPages;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends GemModule {
    
    DaoTasks getTasksDao();    
    
    DaoLocations getLocationsDao();  
    
    DaoCommands getCommandsDao(); 
    
    DaoWebPages getWebPagesDao();
    
    DaoIntellChoice getIntellChoiceDao();
}
