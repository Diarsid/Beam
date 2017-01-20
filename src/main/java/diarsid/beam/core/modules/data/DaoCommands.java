/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;

/**
 *
 * @author Diarsid
 */
public interface DaoCommands {
    
    Optional<ArgumentedCommand> getByExactOriginalOfType(
            Initiator initiator, String original, CommandType type);
    
    List<ArgumentedCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original);
    
    List<ArgumentedCommand> fullSearchByOriginalPattern(
            Initiator initiator, String pattern);
    
    List<ArgumentedCommand> fullSearchByOriginalPatternParts(
            Initiator initiator, List<String> patternParts);
    
    List<ArgumentedCommand> fullSearchByOriginalPatternOfType(
            Initiator initiator, String pattern, CommandType type);
    
    List<ArgumentedCommand> fullSearchByOriginalPatternPartsOfType(
            Initiator initiator, List<String> patternParts, CommandType type);
    
    List<ArgumentedCommand> fullSearchByExtendedPattern(
            Initiator initiator, String pattern);
    
    List<ArgumentedCommand> fullSearchByExtendedPatternParts(
            Initiator initiator, List<String> patternParts);
    
    List<ArgumentedCommand> fullSearchByExtendedPatternOfType(
            Initiator initiator, String pattern, CommandType type);
    
    List<ArgumentedCommand> fullSearchByExtendedPatternPartsOfType(
            Initiator initiator, List<String> patternParts, CommandType type);
    
    boolean save(
            Initiator initiator, ArgumentedCommand command);
    
    boolean delete(
            Initiator initiator, ArgumentedCommand command);
    
    boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original);
    
    boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type);
    
}
