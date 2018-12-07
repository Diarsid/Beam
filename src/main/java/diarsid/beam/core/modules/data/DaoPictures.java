/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Picture;

/**
 *
 * @author Diarsid
 */
public interface DaoPictures extends Dao {
    
    Optional<Picture> getByName(String name) throws DataExtractionException;
    
    boolean save(Picture image) throws DataExtractionException;
    
    boolean removeByName(String name) throws DataExtractionException;
    
    boolean remove(Picture image) throws DataExtractionException;
}
