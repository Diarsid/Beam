/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Attribute;

/**
 *
 * @author Diarsid
 */
public interface DaoKeyValueStorage extends Dao {
    
    Optional<String> get(String key) throws DataExtractionException;
    
    boolean save(String key, String value) throws DataExtractionException;
    
    boolean delete(String key) throws DataExtractionException;
    
    Map<String, String> getAll() throws DataExtractionException;
    
    Optional<Attribute> getAttribute(String key) throws DataExtractionException;
    
    boolean saveAttribute(Attribute attribute) throws DataExtractionException;
    
    boolean deleteAttribute(Attribute attribute) throws DataExtractionException;
    
    Set<Attribute> getAllAttributes() throws DataExtractionException;
}
