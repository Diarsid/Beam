/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.sql.Timestamp;

import diarsid.beam.core.base.analyze.cache.PersistableCacheData;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.Attribute;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.Picture;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.RowConversion;

import static diarsid.beam.core.base.control.io.commands.Commands.createInvocationCommandFrom;
import static diarsid.beam.core.base.control.io.commands.Commands.restoreExecutorCommandFrom;
import static diarsid.beam.core.base.control.io.commands.Commands.restoreInvocationCommandFrom;
import static diarsid.beam.core.domain.entities.Tasks.restoreTask;
import static diarsid.beam.core.domain.entities.WebDirectories.restoreDirectory;
import static diarsid.beam.core.domain.entities.WebPages.restorePage;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
class RowToEntityConversions {
    
    static final RowConversion<Location> ROW_TO_LOCATION  = (row) -> {
        return new Location(
                (String) row.get("loc_name"),
                (String) row.get("loc_path"));
    };
    
    static final RowConversion<ExecutorCommand> ROW_TO_EXECUTOR_COMMAND = (row) -> {
        return restoreExecutorCommandFrom(
                (String) row.get("bat_command_type"), 
                (String) row.get("bat_command_original")
        );
    };
    
    static final RowConversion<InvocationCommand> rowToNewInvocationCommandWithOriginal(
            String original) {        
        return (row) -> {
            return createInvocationCommandFrom(
                (String) row.get("com_type"), 
                original,
                (String) row.get("com_extended")
            );
        };    
    }
    
    static final RowConversion<InvocationCommand> ROW_TO_INVOCATION_COMMAND = (row) -> {
        return restoreInvocationCommandFrom(
                (String) row.get("com_type"), 
                (String) row.get("com_original"),
                (String) row.get("com_extended"));
    };
    
    static final RowConversion<WebPage> ROW_TO_WEBPAGE = (row) -> {
        return restorePage(
                (String) row.get("name"),
                (String) row.get("shortcuts"),
                (String) row.get("url"),
                (int) row.get("ordering"),
                (int) row.get("dir_id"));
    };
    
    static final RowConversion<WebDirectory> ROW_TO_WEBDIRECTORY = (row) -> {
        return restoreDirectory(
                (int) row.get("id"),
                (String) row.get("name"), 
                parsePlace((String) row.get("place")), 
                (int) row.get("ordering"));
    };
    
    static final RowConversion<String> ROW_TO_EXTENDED = (row) -> {
        return (String) row.get("extended");
    };
    
    static final RowConversion<Picture> ROW_TO_IMAGE = (row) -> {
        return new Picture(
                row.get("name", String.class), 
                row.getBytes("bytes"));
    };   
    
    static final RowConversion<Task> ROW_TO_TASK = (row) -> {
        return restoreTask(
                (int) row.get("id"), 
                TaskRepeat.valueOf((String) row.get("type")), 
                ((Timestamp) row.get("time")).toLocalDateTime(), 
                (boolean) row.get("status"), 
                (String) row.get("days"), 
                (String) row.get("hours"), 
                (String) row.get("text"));
    };
    
    static final RowConversion<Attribute> ROW_TO_ATTRIBUTE = (row) -> {
        return new Attribute(
                (String) row.get("key"),
                (String) row.get("value"));
    };
    
    static final RowConversion<PersistableCacheData<Float>> ROW_TO_CACHED_FLOAT = (row) -> {
        return new PersistableCacheData<>(
                row.get("target", String.class), 
                row.get("pattern", String.class), 
                row.get("pair_hash", Long.class),
                row.get("weight", Float.class));
    };
    
    static final RowConversion<PersistableCacheData<Boolean>> ROW_TO_CACHED_BOOLEAN = (row) -> {
        return new PersistableCacheData(
                row.get("target", String.class), 
                row.get("pattern", String.class), 
                row.get("pair_hash", Long.class),
                row.get("isSimilar", Boolean.class));
    };
}
