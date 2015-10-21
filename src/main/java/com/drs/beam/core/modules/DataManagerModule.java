/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules;

import com.drs.beam.core.modules.data.dao.commands.CommandsDao;
import com.drs.beam.core.modules.data.dao.locations.LocationsDao;
import com.drs.beam.core.modules.data.dao.tasks.TasksDao;

/**
 *
 * @author Diarsid
 */
public interface DataManagerModule extends Module {
    
    TasksDao getTasksDao();    
    LocationsDao getLocationsDao();  
    CommandsDao getCommandsDao(); 
    
    static String getModuleName(){
        return "Data Manager Module";
    }
}