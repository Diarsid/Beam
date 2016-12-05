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
public interface ExecutorCommand extends Command {
    
    OperationType getOperation();
    
    String stringifyOriginal();
    
    String stringifyExtended();
}
