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
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoCommands {
    
    Optional<ExtendableCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type);
    
    List<ExtendableCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original);
    
    List<ExtendableCommand> fullSearchByOriginalPattern(
            Initiator initiator, String pattern);
    
    List<ExtendableCommand> searchInOriginalByPatternAndType(
            Initiator initiator, String pattern, CommandType type);
    
    List<ExtendableCommand> fullSearchByExtendedPattern(
            Initiator initiator, String pattern);
    
    List<ExtendableCommand> searchInExtendedByPatternAndType(
            Initiator initiator, String pattern, CommandType type);
    
    boolean save(
            Initiator initiator, ExtendableCommand command);
    
    boolean delete(
            Initiator initiator, ExtendableCommand command);
    
    boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original);
    
    boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type);
    
}
