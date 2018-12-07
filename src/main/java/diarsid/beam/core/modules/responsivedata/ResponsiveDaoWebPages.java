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
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoWebPages;

import static java.util.Collections.emptyList;

/**
 *
 * @author Diarsid
 */
public class ResponsiveDaoWebPages extends BeamCommonResponsiveDao<DaoWebPages> {

    ResponsiveDaoWebPages(DaoWebPages dao, InnerIoEngine ioEngine) {
        super(dao, ioEngine);
    }
    
    public Optional<Integer> findFreeNameNextIndex(Initiator initiator, String name) {
        try {
            return super.dao().findFreeNameNextIndex(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<WebPage> getByExactName(Initiator initiator, String name) { 
        try {
            return super.dao().getByExactName(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public Optional<WebPage> getByUrl(Initiator initiator, String newUrl) {
        try {
            return super.dao().getByUrl(newUrl);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return Optional.empty();
        }
    }
    
    public List<WebPage> findByPattern(Initiator initiator, String pattern) {
        try {
            return super.dao().findByPattern(pattern);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebPage> getAllFromDirectory(Initiator initiator, int directoryId) {
        try {
            return super.dao().getAllFromDirectory(directoryId);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public List<WebPage> getAll(Initiator initiator) {
        try {
            return super.dao().getAll();
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return emptyList();
        }
    }
    
    public boolean save(Initiator initiator, WebPage page) {
        try {
            return super.dao().save(page);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean remove(Initiator initiator, String name) {
        try {
            return super.dao().remove(name);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editName(Initiator initiator, String oldName, String newName) { 
        try {
            return super.dao().editName(oldName, newName);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editShortcuts(Initiator initiator, String name, String newShortcuts) {
        try {
            return super.dao().editShortcuts(name, newShortcuts);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean editUrl(Initiator initiator, String name, String newUrl) {
        try {
            return super.dao().editUrl(name, newUrl);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }    
    
    public boolean movePageFromDirToDir(Initiator initiator, WebPage page, int newDirId) {
        try {
            return super.dao().movePageFromDirToDir(page, newDirId);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
    public boolean updatePageOrdersInDir(Initiator initiator, List<WebPage> pages) {
        try {
            return super.dao().updatePageOrdersInDir(pages);
        } catch (DataExtractionException e) {
            super.responseOn(initiator, e);
            return false;
        }
    }
    
}
