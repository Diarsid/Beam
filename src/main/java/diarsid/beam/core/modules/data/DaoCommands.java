/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoCommands {
    
    Optional<InvocationCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type);
    
    List<InvocationCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original);
    
    List<InvocationCommand> searchInOriginalByPattern(
            Initiator initiator, String pattern);
    
    List<InvocationCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type);
    
    List<InvocationCommand> searchInExtendedByPattern(
            Initiator initiator, String pattern);
    
    List<InvocationCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type);
    
    List<InvocationCommand> searchInExtendedByPatternGroupByExtended(
            Initiator initiator, String pattern);
    
    List<InvocationCommand> searchInExtendedByPatternAndTypeGroupByExtended(
            Initiator initiator, String pattern, CommandType type);
    
    boolean save(
            Initiator initiator, InvocationCommand command);
    
    boolean save(
            Initiator initiator, List<? extends InvocationCommand> commands);
    
    boolean delete(
            Initiator initiator, InvocationCommand command);
    
    boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original);
    
    boolean deleteByExactExtendedOfType(
            Initiator initiator, String extended, CommandType type);
    
    boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type);
    
    boolean deleteByPrefixInExtended(
            Initiator initiator, String prefixInExtended, CommandType type);
}
