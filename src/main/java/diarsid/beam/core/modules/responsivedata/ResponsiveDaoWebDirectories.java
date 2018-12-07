/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.responsivedata;


import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.data.DaoWebDirectories;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoWebDirectories extends BeamCommonResponsiveDao<DaoWebDirectories> {

    ResponsiveDaoWebDirectories(DaoWebDirectories dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<Integer> findFreeNameNextIndex(
            Initiator initiator, String name, WebPlace place) { 
        try {
            return super.dao().findFreeNameNextIndex(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public List<WebDirectoryPages> getAllDirectoriesPages(
            Initiator initiator) {
        try {
            return super.dao().getAllDirectoriesPages();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebDirectoryPages> getAllDirectoriesPagesInPlace(
            Initiator initiator, WebPlace place) {
        try {
            return super.dao().getAllDirectoriesPagesInPlace(place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public Optional<WebDirectoryPages> getDirectoryPagesById(Initiator initiator, int id) { 
        try {
            return super.dao().getDirectoryPagesById(id);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<WebDirectoryPages> getDirectoryPagesByNameAndPlace(
            Initiator initiator, String name, WebPlace place) {
        try {
            return super.dao().getDirectoryPagesByNameAndPlace(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<WebDirectory> getDirectoryByNameAndPlace(
            Initiator initiator, String name, WebPlace place) { 
        try {
            return super.dao().getDirectoryByNameAndPlace(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<WebDirectory> getDirectoryById(Initiator initiator, int id) {
        try {
            return super.dao().getDirectoryById(id);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<Integer> getDirectoryIdByNameAndPlace(
            Initiator initiator, String name, WebPlace place) { 
        try {
            return super.dao().getDirectoryIdByNameAndPlace(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public List<WebDirectory> findDirectoriesByPatternInPlace(
            Initiator initiator, String pattern, WebPlace place) { 
        try {
            return super.dao().findDirectoriesByPatternInPlace(pattern, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebDirectory> findDirectoriesByPatternInAnyPlace(
            Initiator initiator, String pattern) { 
        try {
            return super.dao().findDirectoriesByPatternInAnyPlace(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebDirectory> getAllDirectories(Initiator initiator) {
        try {
            return super.dao().getAllDirectories();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebDirectory> getAllDirectoriesInPlace(Initiator initiator, WebPlace place) {
        try {
            return super.dao().getAllDirectoriesInPlace(place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        } 
    }
    
    public boolean exists(Initiator initiator, String directoryName, WebPlace place) { 
        try {
            return super.dao().exists(directoryName, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean updateWebDirectoryOrders(Initiator initiator, List<WebDirectory> directories) { 
        try {
            return super.dao().updateWebDirectoryOrders(directories);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
        
    public boolean save(Initiator initiator, WebDirectory directory) { 
        try {
            return super.dao().save(directory);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean save(Initiator initiator, String name, WebPlace place) { 
        try {
            return super.dao().save(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }

    public boolean remove(Initiator initiator, String name, WebPlace place) {
        try {
            return super.dao().remove(name, place);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean moveDirectoryToPlace(
            Initiator initiator, String name, WebPlace from, WebPlace to) {
        try {
            return super.dao().moveDirectoryToPlace(name, from, to);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editDirectoryName(
            Initiator initiator, String name, WebPlace place, String newName) {
        try {
            return super.dao().editDirectoryName(name, place, newName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
}
