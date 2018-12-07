/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataExtractionException;

/**
 *
 * @author Diarsid
 */
public interface DaoCommands extends Dao {
    
    Optional<InvocationCommand> getByExactOriginalAndType(
            String original, CommandType type) 
            throws DataExtractionException;
    
    List<InvocationCommand> getByExactOriginalOfAnyType(
            String original) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInOriginalByPattern(
            String pattern) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInOriginalByPatternAndType(
            String pattern, CommandType type) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInExtendedByPattern(
            String pattern) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInExtendedByPatternAndType(
            String pattern, CommandType type) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInExtendedByPatternGroupByExtended(
            String pattern) 
            throws DataExtractionException;
    
    List<InvocationCommand> searchInExtendedByPatternAndTypeGroupByExtended(
            String pattern, CommandType type) 
            throws DataExtractionException;
    
    boolean save(
            InvocationCommand command) 
            throws DataExtractionException;
    
    boolean save(
            List<? extends InvocationCommand> commands) 
            throws DataExtractionException;
    
    boolean delete(
            InvocationCommand command) 
            throws DataExtractionException;
    
    boolean deleteByExactOriginalOfAllTypes(
            String original) 
            throws DataExtractionException;
    
    boolean deleteByExactExtendedOfType(
            String extended, CommandType type) 
            throws DataExtractionException;
    
    boolean deleteByExactOriginalOfType(
            String original, CommandType type) 
            throws DataExtractionException;
    
    boolean deleteByPrefixInExtended(
            String prefixInExtended, CommandType type) 
            throws DataExtractionException;
}
