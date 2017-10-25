/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands;

import static diarsid.beam.core.base.control.io.commands.CommandOperationType.CORE_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.EXECUTOR_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.KEEPER_CREATE_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.KEEPER_EDIT_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.KEEPER_FIND_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.KEEPER_OTHER_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.KEEPER_REMOVE_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.OTHER;

/**
 *
 * @author Diarsid
 */
public enum CommandType {    
    
    OPEN_LOCATION (EXECUTOR_OPERATION),
    OPEN_LOCATION_TARGET (EXECUTOR_OPERATION),    
    RUN_PROGRAM (EXECUTOR_OPERATION),
    CALL_BATCH (EXECUTOR_OPERATION),
    BATCH_PAUSE (EXECUTOR_OPERATION),
    BROWSE_WEBPAGE (EXECUTOR_OPERATION),
    EXECUTOR_DEFAULT (EXECUTOR_OPERATION),
    LIST_LOCATION (EXECUTOR_OPERATION),
    LIST_PATH (EXECUTOR_OPERATION),
    PLUGIN_TASK (EXECUTOR_OPERATION),    
    BROWSE_WEBPANEL (EXECUTOR_OPERATION),
    
    OPEN_NOTES (KEEPER_OTHER_OPERATION),
    OPEN_TARGET_IN_NOTES (KEEPER_OTHER_OPERATION),
    OPEN_PATH_IN_NOTES (KEEPER_OTHER_OPERATION),
    CREATE_NOTE (KEEPER_OTHER_OPERATION),
    
    DELETE_MEM (KEEPER_REMOVE_ENTITY),   
       
    DELETE_PAGE (KEEPER_REMOVE_ENTITY), 
    CREATE_PAGE (KEEPER_CREATE_ENTITY),
    EDIT_PAGE (KEEPER_EDIT_ENTITY),    
    CAPTURE_PAGE_IMAGE (KEEPER_OTHER_OPERATION),
    
    DELETE_WEB_DIR (KEEPER_REMOVE_ENTITY), 
    CREATE_WEB_DIR (KEEPER_CREATE_ENTITY),
    EDIT_WEB_DIR (KEEPER_EDIT_ENTITY),
    
    DELETE_LOCATION (KEEPER_REMOVE_ENTITY),
    CREATE_LOCATION (KEEPER_CREATE_ENTITY),
    EDIT_LOCATION (KEEPER_EDIT_ENTITY),
    
    DELETE_TASK (KEEPER_REMOVE_ENTITY), 
    CREATE_TASK (KEEPER_CREATE_ENTITY),
    EDIT_TASK (KEEPER_EDIT_ENTITY),
    
    DELETE_BATCH (KEEPER_REMOVE_ENTITY), 
    CREATE_BATCH (KEEPER_CREATE_ENTITY),
    EDIT_BATCH (KEEPER_EDIT_ENTITY),
    
    FIND_LOCATION (KEEPER_FIND_ENTITY),
    FIND_PROGRAM (KEEPER_FIND_ENTITY),
    FIND_TASK (KEEPER_FIND_ENTITY),
    FIND_WEBPAGE (KEEPER_FIND_ENTITY),
    FIND_WEBDIRECTORY (KEEPER_FIND_ENTITY),  
    FIND_MEM (KEEPER_FIND_ENTITY),
    FIND_BATCH (KEEPER_FIND_ENTITY),
    FIND_ALL (KEEPER_OTHER_OPERATION),
    SHOW_ALL_LOCATIONS (KEEPER_OTHER_OPERATION),
    SHOW_ALL_PROGRAMS (KEEPER_OTHER_OPERATION),
    SHOW_ALL_TASKS (KEEPER_OTHER_OPERATION),
    SHOW_ALL_WEBPAGES (KEEPER_OTHER_OPERATION),
    SHOW_ALL_WEBDIRECTORIES (KEEPER_OTHER_OPERATION),
    SHOW_ALL_BATCHES (KEEPER_OTHER_OPERATION),
    
    SHOW_WEBPANEL (KEEPER_OTHER_OPERATION),
    SHOW_BOOKMARKS (KEEPER_OTHER_OPERATION),
    
    EXIT (CORE_OPERATION),
    CLOSE_CONSOLE (CORE_OPERATION),
    
    MULTICOMMAND (OTHER),
    
    INCORRECT (OTHER),
    UNDEFINED (OTHER);

    private CommandType() {
        this.operationType = null;
    }
    
    private final CommandOperationType operationType;
    
    private CommandType(CommandOperationType type) {
        this.operationType = type;
    }
    
    public CommandOperationType operationType() {
        return this.operationType;
    }
    
    public boolean isNot(CommandType type) {
        return ! this.equals(type);
    }
    
    public boolean is(CommandType commandType) {
        return this.equals(commandType);
    }
    
    public boolean isDefined() {
        return ( ! this.equals(UNDEFINED) );
    }
    
    public boolean isUndefined() {
        return this.equals(UNDEFINED);
    }
}
