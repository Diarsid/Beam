/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import diarsid.beam.core.base.control.io.commands.exceptions.UndefinedOperationTypeException;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.SeePageCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.valueOf;
import static diarsid.beam.core.domain.entities.BatchPauseCommand.parsePauseCommandFrom;

/**
 *
 * @author Diarsid
 */
public class Commands {
    
    
    private Commands() {
    }
    
    public static ExtendableCommand restoreArgumentedCommandFrom(
            String operationType, 
            String originalArgs, 
            String extendedArgs) {
        return defineCommand(operationType, originalArgs, extendedArgs);
    }

    private static ExtendableCommand defineCommand(
            String operationType, 
            String originalArgs, 
            String extendedArgs) {
        switch ( valueOf(operationType) ) {
            case OPEN_LOCATION : {
                return new OpenLocationCommand(originalArgs, extendedArgs);
            }
            case OPEN_LOCATION_TARGET : {
                return new OpenLocationTargetCommand(originalArgs, extendedArgs);
            }
            case RUN_PROGRAM : {
                return new RunProgramCommand(originalArgs, extendedArgs);
            }
            case SEE_WEBPAGE : {
                return new SeePageCommand(originalArgs, extendedArgs);
            }
            case CALL_BATCH : {
                return new CallBatchCommand(originalArgs, extendedArgs);
            }
            case BATCH_PAUSE : {
                return parsePauseCommandFrom(extendedArgs);
            }
            case EXECUTOR_DEFAULT : {
                return new ExecutorDefaultCommand(originalArgs, extendedArgs);
            }
            default : {
                throw new UndefinedOperationTypeException();
            }
        }  
    }
}
