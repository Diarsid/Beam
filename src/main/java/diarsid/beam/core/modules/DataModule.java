/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.StopableBeamModule;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoIntellChoice;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.data.HandlerLocations;
import diarsid.beam.core.modules.data.HandlerWebPages;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends StopableBeamModule {
    
    DaoTasks getTasksDao();    
    
    HandlerLocations getLocationsHandler();  
    
    DaoCommands getCommandsDao(); 
    
    HandlerWebPages getWebPagesHandler();
    
    DaoIntellChoice getIntellChoiceDao();
}
