/*
 * project: Beam
 * author: Diarsid
 */

package diarsid.beam.core.modules.data.base.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class DataBaseInitializer {    
    
    private final IoInnerModule ioEngine;    
    
    DataBaseInitializer(IoInnerModule io) {
        this.ioEngine = io;
    }
    
    void createTable(
            DataBaseModel.TableInfo tableInfo, 
            DataBase db, 
            List<String> messagePool) {
        
        try (Connection con = db.connect()) {
            con.createStatement().executeUpdate(tableInfo.getSqlScript());
            messagePool.add(
                    "Table '"+tableInfo.getTableName()+"' has been created.");
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLException: create table '" + 
                            tableInfo.getTableName() + "' failure.",
                    "Program will be closed.");
            e.printStackTrace();
            throw new ModuleInitializationException();
        }
    }
}
