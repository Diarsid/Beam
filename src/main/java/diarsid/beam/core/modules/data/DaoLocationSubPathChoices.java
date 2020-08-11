/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.LocationSubPath;

/**
 *
 * @author Diarsid
 */
public interface DaoLocationSubPathChoices extends Dao {
    
    boolean saveSingle(
            LocationSubPath subPath, 
            String pattern) throws DataExtractionException;
    
    boolean saveWithVariants(
            LocationSubPath subPath, 
            String pattern, 
            Variants variants) throws DataExtractionException;
    
    boolean isChoiceExistsForSingle(
            LocationSubPath subPath, 
            String pattern) throws DataExtractionException;
    
    Optional<LocationSubPath> getChoiceFor(
            String pattern, 
            Variants variants) throws DataExtractionException;
    
    VoidFlow remove(
            LocationSubPath subPath) throws DataExtractionException;
}
