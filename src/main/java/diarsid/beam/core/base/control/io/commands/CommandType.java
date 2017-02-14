/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands;

import static diarsid.beam.core.base.control.io.commands.CommandOperationType.CORE_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.CREATE_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.EDIT_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.EXECUTOR_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.FIND_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.OTHER;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.REMOVE_ENTITY;

/**
 *
 * @author Diarsid
 */
public enum CommandType {    
    
    OPEN_LOCATION (EXECUTOR_OPERATION),
    OPEN_PATH (EXECUTOR_OPERATION),    
    RUN_PROGRAM (EXECUTOR_OPERATION),
    START_PROGRAM (EXECUTOR_OPERATION),
    STOP_PROGRAM (EXECUTOR_OPERATION),
    CALL_BATCH (EXECUTOR_OPERATION),
    BATCH_PAUSE (EXECUTOR_OPERATION),
    SEE_WEBPAGE (EXECUTOR_OPERATION),
    EXECUTOR_DEFAULT (EXECUTOR_OPERATION),
    
    OPEN_NOTES (EXECUTOR_OPERATION),
    OPEN_TARGET_IN_NOTE (EXECUTOR_OPERATION),
    OPEN_PATH_IN_NOTE (EXECUTOR_OPERATION),
    
    DELETE_MEM (REMOVE_ENTITY),   
       
    DELETE_PAGE (REMOVE_ENTITY), 
    CREATE_PAGE (CREATE_ENTITY),
    EDIT_PAGE (EDIT_ENTITY),
    
    DELETE_PAGE_DIR (REMOVE_ENTITY), 
    CREATE_PAGE_DIR (CREATE_ENTITY),
    EDIT_PAGE_DIR (EDIT_ENTITY),
    
    DELETE_LOCATION (REMOVE_ENTITY),
    CREATE_LOCATION (CREATE_ENTITY),
    EDIT_LOCATION (EDIT_ENTITY),
    
    DELETE_TASK (REMOVE_ENTITY), 
    CREATE_TASK (CREATE_ENTITY),
    EDIT_TASK (EDIT_ENTITY),
    
    DELETE_REMINDER (REMOVE_ENTITY), 
    CREATE_REMINDER (CREATE_ENTITY),
    EDIT_REMINDER (EDIT_ENTITY),
    
    DELETE_EVENT (REMOVE_ENTITY), 
    CREATE_EVENT (CREATE_ENTITY),
    EDIT_EVENT (EDIT_ENTITY),
    
    DELETE_BATCH (REMOVE_ENTITY), 
    CREATE_BATCH (CREATE_ENTITY),
    EDIT_BATCH (EDIT_ENTITY),
    
    LIST_LOCATION (EXECUTOR_OPERATION),
    LIST_PATH (EXECUTOR_OPERATION),
    
    FIND_LOCATION (FIND_ENTITY),
    FIND_PROGRAM (FIND_ENTITY),
    FIND_TASK (FIND_ENTITY),
    FIND_EVENT (FIND_ENTITY),
    FIND_REMINDER (FIND_ENTITY),
    FIND_PAGE (FIND_ENTITY),
    FIND_WEBDIRECTORY (FIND_ENTITY),  
    FIND_MEM (FIND_ENTITY),
    FIND_BATCH (FIND_ENTITY),
    
    EXIT (CORE_OPERATION),
    CLOSE_CONSOLE (CORE_OPERATION),
    
    UNDEFINED (OTHER);
    
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
    
    public static boolean isDefined(CommandType type) {
        return ( ! type.equals(UNDEFINED) );
    }
}
