/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import java.util.ArrayList;
import java.util.List;


public class H2DataBaseModel implements SqlDataBaseModel {
    
    private final List<SqlTable> tables;
    
    public H2DataBaseModel() {
        this.tables = new ArrayList<>();
        
        SqlTable locations = new H2SqlTable(
                "locations", 
                "CREATE TABLE locations (" +
                "loc_name   VARCHAR(300)    NOT NULL PRIMARY KEY," +
                "loc_path   VARCHAR(1000)    NOT NULL)", 
                2);
        
        SqlTable batches = new H2SqlTable(
                "batches", 
                "CREATE TABLE batches ( " +
                "bat_name   VARCHAR(300)    NOT NULL PRIMARY KEY )", 
                1);
        
        SqlTable batchCommands = new H2SqlTable(
                "batch_commands", 
                "CREATE TABLE batch_commands (" +
                "bat_name               VARCHAR(300)    NOT NULL," +
                "bat_command_order      INTEGER         NOT NULL, " +
                "bat_command_type       VARCHAR(50)     NOT NULL, " +
                "bat_command_original   VARCHAR         NOT NULL, " +
                "bat_command_extended   VARCHAR         NOT NULL,  " +
                "       PRIMARY KEY (bat_name, bat_command_order) )", 
                5);
        
        SqlTable keyValue = new H2SqlTable(
                "key_value", 
                "CREATE TABLE key_value (" +
                "key    VARCHAR     NOT NULL PRIMARY KEY," +
                "value  VARCHAR     NOT NULL)", 
                2);
        
        SqlTable commands = new H2SqlTable(
                "commands", 
                "CREATE TABLE commands (" +
                "com_type       VARCHAR     NOT NULL, " +
                "com_original   VARCHAR     NOT NULL," +
                "com_extended   VARCHAR     NOT NULL, " +
                "       PRIMARY KEY (com_type, com_original) )", 
                3);
        
        this.tables.add(locations);
        this.tables.add(batches); 
        this.tables.add(batchCommands);
        this.tables.add(keyValue);
        this.tables.add(commands);
    }

    @Override
    public List<SqlTable> getTables() {
        return this.tables;
    }
}
