/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.commands;

/**
 *
 * @author Diarsid
 */
public enum CommandType {    
    
    OPEN_LOCATION,
    OPEN_PATH,    
    RUN_PROGRAM,
    RUN_MARKED_PROGRAM,
    CALL_BATCH,
    SEE_WEBPAGE,
    EXECUTOR_DEFAULT,
    
    OPEN_NOTES,
    OPEN_TARGET_IN_NOTE,
    OPEN_PATH_IN_NOTE,
    
    DELETE_MEM,   
       
    DELETE_PAGE, 
    CREATE_PAGE,
    EDIT_PAGE,
    
    DELETE_PAGE_DIR, 
    CREATE_PAGE_DIR,
    EDIT_PAGE_DIR,
    
    DELETE_LOCATION,
    CREATE_LOCATION,
    EDIT_LOCATION,
    
    DELETE_TASK, 
    CREATE_TASK,
    EDIT_TASK,
    
    DELETE_REMINDER, 
    CREATE_REMINDER,
    EDIT_REMINDER,
    
    DELETE_EVENT, 
    CREATE_EVENT,
    EDIT_EVENT,
    
    DELETE_BATCH, 
    CREATE_BATCH,
    EDIT_BATCH,
    
    LIST_LOCATION,
    LIST_PATH,
    
    FIND_LOCATION,
    FIND_TASK,
    FIND_EVENT,
    FIND_REMINDER,
    FIND_PAGE,
    FIND_WEBDIRECTORY,  
    FIND_MEM,
    FIND_BATCH,
    
    EXIT,
    CLOSE_CONSOLE,
    
    UNDEFINED;
    
    public static boolean isDefined(CommandType type) {
        return ( type != UNDEFINED );
    }
}
