/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.domain.Attribute;

/**
 *
 * @author Diarsid
 */
public interface DaoKeyValueStorage {
    
    Optional<String> get(String key);
    
    boolean save(String key, String value);
    
    boolean delete(String key);
    
    Map<String, String> getAll();
    
    Optional<Attribute> getAttribute(String key);
    
    boolean saveAttribute(Attribute attribute);
    
    boolean deleteAttribute(Attribute attribute);
    
    Set<Attribute> getAllAttributes();
}
