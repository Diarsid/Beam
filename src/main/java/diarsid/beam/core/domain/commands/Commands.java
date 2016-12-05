/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands;

import diarsid.beam.core.domain.commands.executor.OpenLocationCommand;
import diarsid.beam.core.domain.commands.executor.RunMarkedProgramCommand;
import diarsid.beam.core.domain.commands.executor.CallBatchCommand;
import diarsid.beam.core.domain.commands.executor.SeePageCommand;
import diarsid.beam.core.domain.commands.executor.RunProgramCommand;
import diarsid.beam.core.domain.commands.executor.OpenTargetInLocationCommand;
import diarsid.beam.core.domain.commands.exceptions.UndefinedOperationTypeException;

import static diarsid.beam.core.domain.commands.OperationType.OPEN_LOCATION;
import static diarsid.beam.core.domain.commands.OperationType.valueOf;

/**
 *
 * @author Diarsid
 */
public class Commands {
    
    
    private Commands() {
    }
    
    public static ExecutorCommand restoreFrom(
            String operationType, 
            String originalArgs, 
            String extendedArgs,
            String... attributes) {
        switch ( valueOf(operationType) ) {
            case OPEN_LOCATION : {
                return new OpenLocationCommand(originalArgs, extendedArgs);
            }
            case OPEN_TARGET_IN_LOCATION : {
                return new OpenTargetInLocationCommand(originalArgs, extendedArgs);
            }
            case RUN_PROGRAM : {
                return new RunProgramCommand(originalArgs, extendedArgs);
            }
            case RUN_MARKED_PROGRAM : {
                return new RunMarkedProgramCommand(originalArgs, extendedArgs, attributes[0]);
            }
            case SEE_WEBPAGE : {
                return new SeePageCommand(originalArgs, extendedArgs, attributes);
            }
            case CALL_BATCH : {
                return new CallBatchCommand(originalArgs, extendedArgs);
            }
            default : {
                throw new UndefinedOperationTypeException();
            }
        }  
    }
    
}
