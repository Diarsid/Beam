/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules;

import diarsid.beam.core.StoppableBeamModule;

import old.diarsid.beam.core.modules.data.DaoCommandsBatches;
import old.diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;
import old.diarsid.beam.core.modules.data.DaoExecutorIntelligentChoices;
import old.diarsid.beam.core.modules.data.DaoTasks;
import old.diarsid.beam.core.modules.data.HandlerLocations;
import old.diarsid.beam.core.modules.data.HandlerWebPages;
import old.diarsid.beam.core.modules.data.DaoActionChoice;

/**
 *
 * @author Diarsid
 */
public interface DataModule extends StoppableBeamModule {
    
    DaoTasks getTasksDao();    
    
    HandlerLocations getLocationsHandler();  
    
    DaoCommandsBatches getCommandsDao(); 
    
    HandlerWebPages getWebPagesHandler();
    
    DaoExecutorIntelligentChoices getIntellChoiceDao();
    
    DaoExecutorConsoleCommands getConsoleCommandsDao();
    
    DaoActionChoice getActionsChoiceDao();
}
