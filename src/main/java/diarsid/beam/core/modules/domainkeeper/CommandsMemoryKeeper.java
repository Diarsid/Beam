/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsMemoryKeeper {
    
    ValueOperation<InvocationCommand> findStoredCommandOfAnyType(
            Initiator initiator, String original); 
    
//    ValueOperation<InvocationCommand> findStoredCommandByPatternOfAnyType(
//            Initiator initiator, String pattern);
    
    void tryToExtendCommand(
            Initiator initiator, InvocationCommand command);
    
    void tryToExtendCommandByPattern(
            Initiator initiator, InvocationCommand command);
    
    void save(
            Initiator initiator, InvocationCommand command);
    
    void remove(
            Initiator initiator, InvocationCommand command);
    
    void removeByExactExtendedAndType(
            Initiator initiator, String extended, CommandType type);
    
    void removeByExactExtendedLocationPrefixInPath(
            Initiator initiator, String extended);
}
