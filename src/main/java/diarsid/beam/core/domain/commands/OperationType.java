/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.commands;

/**
 *
 * @author Diarsid
 */
public enum OperationType {    
    
    OPEN_LOCATION,
    OPEN_TARGET_IN_LOCATION,    
    RUN_PROGRAM,
    RUN_MARKED_PROGRAM,
    CALL_BATCH,
    SEE_WEBPAGE,
    EXECUTOR_DEFAULT,
    
    UNDEFINED;
    
//    public static OperationType defineTypeOf(String operation) {
//        switch ( operation.toLowerCase().trim() ) {
//            case "open"
//        }
//    }
}
