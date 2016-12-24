/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.base.builder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.exceptions.ModuleInitializationException;

import old.diarsid.beam.core.modules.IoInnerModule;

import diarsid.beam.core.modules.data.DataBase;

/**
 *
 * @author Diarsid
 */
class DataBaseVerifier {
    
    private final DataBaseInitializer initializer;
    private final IoInnerModule ioEngine;
    private final DataBaseModel dataModel;
    
    DataBaseVerifier(
            IoInnerModule io, DataBaseInitializer init, DataBaseModel model) {
        this.ioEngine = io;
        this.initializer = init;
        this.dataModel = model;
    }    
    
    void verifyDataBase(DataBase db) {
        List<String> existingTables = this.getExistingTables(db);
        List<String> messagePool = new ArrayList<>();
        for (String table : dataModel.getTableNames()) {
            if (!existingTables.contains(table)) {
                messagePool.add("Table '"+table+"' does not exists.");
                this.initializer.createTable(
                        dataModel.getTable(table), 
                        db, 
                        messagePool);
                messagePool.add(" ");
            }
        }
        if (messagePool.size() > 0){
            this.ioEngine.reportMessage(
                    messagePool.toArray(new String[messagePool.size()]));
        }
    }
    
    private List<String> getExistingTables(DataBase db) {        
        try (Connection con = db.connect();) {
            DatabaseMetaData metadata = con.getMetaData();
            ResultSet rs = metadata.getTables(null, null, "%", null);
            List<String> existingTables = new ArrayList<>();
            while (rs.next()) {
                existingTables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            rs.close();
            return existingTables;            
        } catch (SQLException e) {
            this.ioEngine.reportExceptionAndExitLater(e, 
                    "SQLExceptions: get existing data base tables failure.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
    }
}
