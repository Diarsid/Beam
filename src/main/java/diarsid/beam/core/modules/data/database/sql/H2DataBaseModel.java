/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.database.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class H2DataBaseModel implements SqlDataBaseModel {
    
    private final List<SqlTable> tables;
    
    public H2DataBaseModel() {
        this.tables = new ArrayList<>();
        
        SqlTable locations = new H2SqlTable(
                "locations", 
                "CREATE TABLE locations (" +
                "loc_name   VARCHAR(300)    NOT NULL PRIMARY KEY," +
                "loc_path   VARCHAR(1000)   NOT NULL)", 
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
                "       PRIMARY KEY (bat_name, bat_command_order) )", 
                4);
        
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
        
        SqlTable commandsChoices = new H2SqlTable(
                "commands_choices", 
                "CREATE TABLE commands_choices (" +
                "com_original           VARCHAR     NOT NULL," +
                "com_type               VARCHAR     NOT NULL, " +         
                "com_variants_stamp     VARCHAR     NOT NULL, " +
                "       PRIMARY KEY (com_original) )", 
                3);
        
        SqlTable tasks = new H2SqlTable(
                "tasks", 
                "CREATE TABLE tasks (" + 
                "id     INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY, " + 
                "time   TIMESTAMP   NOT NULL, " +
                "status BOOLEAN     NOT NULL, " + 
                "text   VARCHAR     NOT NULL, " +
                "type   VARCHAR     NOT NULL, " +
                "days   VARCHAR     NOT NULL, " +
                "hours  VARCHAR     NOT NULL )", 
                7);
        
        SqlTable webPages = new H2SqlTable(
                "web_pages", 
                "CREATE TABLE web_pages ( " +
                "name       VARCHAR     NOT NULL PRIMARY KEY, " +
                "url        VARCHAR     NOT NULL, " +
                "shortcuts  VARCHAR     NOT NULL, " +
                "ordering   INTEGER     NOT NULL, " +
                "dir_id     INTEGER     NOT NULL ) ", 
                5);
        
        SqlTable webDirs = new H2SqlTable(
                "web_directories", 
                "CREATE TABLE web_directories ( " +
                "id         INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "name       VARCHAR     NOT NULL, " +
                "place      VARCHAR     NOT NULL, " +
                "ordering   INTEGER     NOT NULL )", 
                4);
        
        SqlTable images = new H2SqlTable(
                "images", 
                "CREATE TABLE images (" +
                "name   VARCHAR     NOT NULL PRIMARY KEY, " +
                "bytes  BLOB        NOT NULL )", 
                2);
        
        this.tables.add(locations);
        this.tables.add(batches); 
        this.tables.add(batchCommands);
        this.tables.add(commandsChoices);
        this.tables.add(keyValue);
        this.tables.add(commands);
        this.tables.add(tasks);
        this.tables.add(webPages);
        this.tables.add(webDirs);
        this.tables.add(images);
    }

    @Override
    public List<SqlTable> tables() {
        return this.tables;
    }

    @Override
    public Optional<SqlTable> getByName(String name) {
        return this.tables
                .stream()
                .filter(table -> table.name().equalsIgnoreCase(name))
                .findFirst();
    }
}
