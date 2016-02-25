/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.core.modules.data.base.builder;

import diarsid.beam.core.modules.data.DataBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import diarsid.beam.core.modules.IoInnerModule;

/**
 *
 * @author Diarsid
 */
class DataBaseInitializer {    
    // Fields =============================================================================  
    private final IoInnerModule ioEngine;
    
    
    // Constructors =======================================================================
    DataBaseInitializer(IoInnerModule io) {
        this.ioEngine = io;
    }

    // Methods ============================================================================    
    
    void createTable(DataBaseModel.TableInfo tableInfo, DataBase db, List<String> messagePool){
        try (Connection con = db.connect()){
            con.createStatement().executeUpdate(tableInfo.getSqlScript());
            messagePool.add("Table '"+tableInfo.getTableName()+"' has been created.");
        } catch (SQLException e){
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: create table '"+tableInfo.getTableName()+"' failure.",
                    "Program will be closed.");
            e.printStackTrace();
            throw new ModuleInitializationException();
        }
    }
}
