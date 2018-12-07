/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface DaoWebDirectories extends Dao {
    
    Optional<Integer> findFreeNameNextIndex(
            String name, WebPlace place) 
            throws DataExtractionException;
    
    List<WebDirectoryPages> getAllDirectoriesPages() 
            throws DataExtractionException;
    
    List<WebDirectoryPages> getAllDirectoriesPagesInPlace(
            WebPlace place) 
            throws DataExtractionException;
    
    Optional<WebDirectoryPages> getDirectoryPagesById(
            int id) 
            throws DataExtractionException;
    
    Optional<WebDirectoryPages> getDirectoryPagesByNameAndPlace(
            String name, WebPlace place) 
            throws DataExtractionException;
    
    Optional<WebDirectory> getDirectoryByNameAndPlace(
            String name, WebPlace place) 
            throws DataExtractionException;
    
    Optional<WebDirectory> getDirectoryById(
            int id) 
            throws DataExtractionException;
    
    Optional<Integer> getDirectoryIdByNameAndPlace(
            String name, WebPlace place) 
            throws DataExtractionException;
    
    List<WebDirectory> findDirectoriesByPatternInPlace(
            String pattern, WebPlace place) 
            throws DataExtractionException;
    
    List<WebDirectory> findDirectoriesByPatternInAnyPlace(
            String pattern) 
            throws DataExtractionException;
    
    List<WebDirectory> getAllDirectories() 
            throws DataExtractionException;
    
    List<WebDirectory> getAllDirectoriesInPlace(
            WebPlace place) 
            throws DataExtractionException;
    
    boolean exists(
            String directoryName, WebPlace place) 
            throws DataExtractionException;
    
    boolean updateWebDirectoryOrders(
            List<WebDirectory> directories) 
            throws DataExtractionException;
        
    boolean save(
            WebDirectory directory) 
            throws DataExtractionException;
    
    boolean save(
            String name, WebPlace place) 
            throws DataExtractionException;

    boolean remove(
            String name, WebPlace place) 
            throws DataExtractionException;
    
    boolean moveDirectoryToPlace(
            String name, WebPlace from, WebPlace to) 
            throws DataExtractionException;
    
    boolean editDirectoryName(
            String name, WebPlace place, String newName) 
            throws DataExtractionException;
    
}
