/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataExtractionException;

/**
 *
 * @author Diarsid
 */
public interface DaoPatternChoices extends Dao {
    
    boolean hasMatchOf(
            String original, String extended, Variants variants) 
            throws DataExtractionException;
    
    Optional<String> findChoiceFor(
            String original, Variants variants)
            throws DataExtractionException;
    
    boolean save(
            String original, String extended, Variants variants)
            throws DataExtractionException;
    
    boolean save(
            InvocationCommand command, Variants variants)
            throws DataExtractionException;
    
    boolean delete(
            String original)
            throws DataExtractionException;
    
    boolean delete(
            InvocationCommand command)
            throws DataExtractionException;
}
