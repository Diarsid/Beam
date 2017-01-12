/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.commands;

import diarsid.beam.core.control.io.commands.exceptions.WrongCommandOperationTypeException;

/**
 *
 * @author Diarsid
 */
public enum CommandOperationType {
    
    REMOVE_ENTITY,
    EDIT_ENTITY,
    CREATE_ENTITY,
    FIND_ENTITY,
    EXECUTOR_OPERATION,
    CORE_OPERATION,
    OTHER;
    
    public boolean isNot(CommandOperationType type) {
        return ! this.equals(type);
    }
    
    public static void onlyIfCommandHasAppropriateOperationType(
            CommandType command, CommandOperationType operation) {
        if ( command.operationType().isNot(operation) ) {
            throw new WrongCommandOperationTypeException(
                    command.name() + " is not " + operation.name() + " operation type command.");
        }
    }
}
