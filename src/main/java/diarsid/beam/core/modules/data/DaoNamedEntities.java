/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.NamedEntity;

/**
 *
 * @author Diarsid
 */
public interface DaoNamedEntities extends Dao {
    
    Optional<NamedEntity> getByExactName(String exactName) throws DataExtractionException;
    
    List<NamedEntity> getEntitiesByNamePattern(String namePattern) throws DataExtractionException;
    
    List<NamedEntity> getAll() throws DataExtractionException;
}
