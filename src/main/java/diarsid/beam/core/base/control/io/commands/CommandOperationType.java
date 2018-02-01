/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands;

import diarsid.beam.core.base.control.io.commands.exceptions.WrongCommandOperationTypeException;

/**
 *
 * @author Diarsid
 */
public enum CommandOperationType {
    
    KEEPER_REMOVE_ENTITY,
    KEEPER_EDIT_ENTITY,
    KEEPER_CREATE_ENTITY,
    KEEPER_FIND_ENTITY,
    KEEPER_OTHER_OPERATION,
    EXECUTOR_OPERATION,
    EXECUTOR_INVOCATION,
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
