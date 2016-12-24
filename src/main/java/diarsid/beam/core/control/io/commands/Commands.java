/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import diarsid.beam.core.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.control.io.commands.executor.RunMarkedProgramCommand;
import diarsid.beam.core.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.control.io.commands.executor.SeePageCommand;
import diarsid.beam.core.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.io.commands.exceptions.UndefinedOperationTypeException;

import static diarsid.beam.core.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.valueOf;

/**
 *
 * @author Diarsid
 */
public class Commands {
    
    
    private Commands() {
    }
    
    public static ArgumentedCommand restoreFrom(
            String operationType, 
            String originalArgs, 
            String extendedArgs,
            String... attributes) {
        switch ( valueOf(operationType) ) {
            case OPEN_LOCATION : {
                return new OpenLocationCommand(originalArgs, extendedArgs);
            }
            case OPEN_PATH : {
                return new OpenPathCommand(originalArgs, extendedArgs);
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
