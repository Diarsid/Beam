/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebPage;

/**
 *
 * @author Diarsid
 */
public interface DaoWebPages extends Dao {
    
    Optional<Integer> findFreeNameNextIndex(
            String name) 
            throws DataExtractionException;
    
    Optional<WebPage> getByExactName(
            String name) 
            throws DataExtractionException;
    
    Optional<WebPage> getByUrl(
            String newUrl) 
            throws DataExtractionException;
    
    List<WebPage> findByPattern(
            String pattern) 
            throws DataExtractionException;
    
    List<WebPage> getAllFromDirectory(
            int directoryId) 
            throws DataExtractionException;
    
    List<WebPage> getAll() 
            throws DataExtractionException;
    
    boolean save(
            WebPage page) 
            throws DataExtractionException;
    
    boolean remove(
            String name) 
            throws DataExtractionException;
    
    boolean editName(
            String oldName, String newName) 
            throws DataExtractionException;
    
    boolean editShortcuts(
            String name, String newShortcuts) 
            throws DataExtractionException;
    
    boolean editUrl(
            String name, String newUrl) 
            throws DataExtractionException;    
    
    boolean movePageFromDirToDir(
            WebPage page, int newDirId) 
            throws DataExtractionException;
    
    boolean updatePageOrdersInDir(
            List<WebPage> pages) 
            throws DataExtractionException;
}
