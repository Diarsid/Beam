/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsMemoryKeeper {
    
    ValueOperation<InvocationCommand> findStoredCommandOfAnyType(
            Initiator initiator, String original); 
    
    ValueOperation<InvocationCommand> findStoredCommandByPatternAndType(
            Initiator initiator, String pattern, CommandType type);
    
    ValueOperation<List<InvocationCommand>> findMems(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation remove(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation tryToExtendCommand(
            Initiator initiator, InvocationCommand command);
    
    VoidOperation tryToExtendCommandByPattern(
            Initiator initiator, InvocationCommand command);
    
    void save(
            Initiator initiator, InvocationCommand command);
    
    void remove(
            Initiator initiator, InvocationCommand command);
    
    void removeByExactOriginalAndType(
            Initiator initiator, String original, CommandType type);
    
    void removeByExactExtendedAndType(
            Initiator initiator, String extended, CommandType type);
    
    void removeByExactExtendedLocationPrefixInPath(
            Initiator initiator, String extended);
}
