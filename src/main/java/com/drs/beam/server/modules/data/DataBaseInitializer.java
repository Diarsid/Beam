/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.server.modules.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.drs.beam.server.modules.ModuleInitializationException;
import com.drs.beam.server.modules.data.base.DataBase;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 *
 * @author Diarsid
 */
class DataBaseInitializer {    
    // Fields =============================================================================  
    private final InnerIOModule ioEngine;
    
    
    // Constructors =======================================================================
    DataBaseInitializer(InnerIOModule io) {
        this.ioEngine = io;
    }

    // Methods ============================================================================    
    
    void createTable(DataBaseModel.TableInfo tableInfo, DataBase db, List<String> messagePool){
        try (Connection con = db.connect()){
            con.createStatement().executeUpdate(tableInfo.getSqlScript());
            messagePool.add("Table '"+tableInfo.getTableName()+"' has been created.");
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: create table '"+tableInfo.getTableName()+" failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }
}
