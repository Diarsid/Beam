/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Diarsid
 */
class DataBaseModel {
    // Fields =============================================================================
    private final Map<String, TableInfo> tables;
    // Constructors =======================================================================

    DataBaseModel() {
        this.tables = new HashMap<>();
        TableInfo tasks = new TableInfo(
                "tasks", 
                "CREATE TABLE tasks (" +
                "t_id       INTEGER         NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "t_time     CHARACTER(16)   NOT NULL, " +
                "t_content  VARCHAR(200)    NOT NULL, " +
                "t_type     VARCHAR(5)      NOT NULL," +
                "t_status   BOOLEAN         NOT NULL)",
                5);
        this.tables.put(tasks.name, tasks);
        
        TableInfo locations = new TableInfo(
                "locations", 
                "CREATE TABLE locations (" +
                "location_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
                "location_path   VARCHAR(300)    NOT NULL)",
                2);
        this.tables.put(locations.name, locations);
        
        TableInfo commands = new TableInfo(
                "commands", 
                "CREATE TABLE commands (" +
                "command_name   VARCHAR(20)     NOT NULL PRIMARY KEY, " +
                "command_text   VARCHAR(300)    NOT NULL)", 
                2);
        this.tables.put(commands.name, commands);
    }
    
    // Methods ============================================================================
    
    List<String> getTableNames(){
        return new ArrayList<>(this.tables.keySet());
    }
    
    TableInfo getTable(String name){
        return this.tables.get(name);
    }
    
    class TableInfo{
        private final String name;
        private final String creationSqlScript;
        private final int columnsQty;
        
        private TableInfo(String name, String script, int columnsQty){
            this.name = name;
            this.creationSqlScript = script;
            this.columnsQty = columnsQty;
        }
        
        String getTableName(){
            return this.name;
        }
        
        String getSqlScript(){
            return this.creationSqlScript;
        }
        
        int getColQty(){
            return this.columnsQty;
        }
    }
}
