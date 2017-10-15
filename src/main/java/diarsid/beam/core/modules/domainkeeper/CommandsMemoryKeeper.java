/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Variants.View;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsMemoryKeeper {
    
    ValueFlow<InvocationCommand> findStoredCommandOfAnyType(
            Initiator initiator, String original); 
    
    ValueFlow<InvocationCommand> findStoredCommandByPatternAndType(
            Initiator initiator, String pattern, CommandType type, View view);
    
    ValueFlow<List<InvocationCommand>> findMems(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow remove(
            Initiator initiator, ArgumentsCommand command);
    
    VoidFlow tryToExtendCommand(
            Initiator initiator, InvocationCommand command);
    
    VoidFlow tryToExtendCommandByPattern(
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
