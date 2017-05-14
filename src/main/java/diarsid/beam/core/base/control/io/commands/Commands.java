/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.exceptions.UndefinedOperationTypeException;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.valueOf;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.STORED;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_NOT_FOUND;
import static diarsid.beam.core.domain.entities.BatchPauseCommand.parsePauseCommandFrom;

/**
 *
 * @author Diarsid
 */
public class Commands {
    
    
    private Commands() {
    }
    
    public static InvocationCommand restoreInvocationCommandFrom(
            String operationType, 
            String originalArgs, 
            String extendedArgs) {
        switch ( valueOf(operationType) ) {
            case OPEN_LOCATION : {
                return new OpenLocationCommand(
                        originalArgs, extendedArgs, STORED, TARGET_NOT_FOUND);
            }
            case OPEN_LOCATION_TARGET : {
                return new OpenLocationTargetCommand(
                        originalArgs, extendedArgs, STORED, TARGET_NOT_FOUND);
            }
            case RUN_PROGRAM : {
                return new RunProgramCommand(
                        originalArgs, extendedArgs, STORED, TARGET_NOT_FOUND);
            }
            case BROWSE_WEBPAGE : {
                return new BrowsePageCommand(
                        originalArgs, extendedArgs, STORED, TARGET_NOT_FOUND);
            }
            case CALL_BATCH : {
                return new CallBatchCommand(
                        originalArgs, extendedArgs, STORED, TARGET_NOT_FOUND);
            }
            default : {
                throw new UndefinedOperationTypeException();
            }
        }
    }
    
    public static ExecutorCommand restoreExecutorCommandFrom(
            String operationType, 
            String originalArgument) {
        switch ( valueOf(operationType) ) {
            case OPEN_LOCATION : {
                return new OpenLocationCommand(originalArgument);
            }
            case OPEN_LOCATION_TARGET : {
                return new OpenLocationTargetCommand(originalArgument);
            }
            case RUN_PROGRAM : {
                return new RunProgramCommand(originalArgument);
            }
            case BROWSE_WEBPAGE : {
                return new BrowsePageCommand(originalArgument);
            }
            case CALL_BATCH : {
                return new CallBatchCommand(originalArgument);
            }
            case BATCH_PAUSE : {
                return parsePauseCommandFrom(originalArgument);
            }
            default : {
                throw new UndefinedOperationTypeException();
            }
        }
    }
}
