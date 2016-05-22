/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data;

import java.util.Collections;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.IoInnerModule;

import static diarsid.beam.core.entities.WebPage.WEB_NAME_REGEXP;

/**
 *
 * @author Diarsid
 */
class HandlerWorkerWebPages implements HandlerWebPages {
    
    private final DaoWebPages dao;
    private final IoInnerModule ioEngine;
    
    HandlerWorkerWebPages(IoInnerModule io, DaoWebPages dao) {
        this.ioEngine = io;
        this.dao = dao;
    }
    
    @Override
    public boolean saveWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser) {
        
        name = name.trim().toLowerCase();
        if ( ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Page name is invalid.");
            return false;
        }
        if ( name.endsWith("/") ) {
            name = name.substring(0, name.length()-1);
        }
        directory = directory.trim().toLowerCase();
        if ( ! directory.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return false;
        }
        browser = browser.trim().toLowerCase();
        return this.dao.saveWebPage(WebPage.newPage(
                name, shortcuts, urlAddress, placement, directory, browser));
    }
    
    @Override
    public boolean deleteWebPage(String name, String dir, WebPagePlacement place) {        
        if ( ! dir.matches(WEB_NAME_REGEXP) || ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Page or directory name is invalid.");
            return false;
        }    
        name = name.trim().toLowerCase();
        return this.dao.deleteWebPage(name, dir, place);
    }
    
    @Override
    public List<String> getAllDirectoriesInPlacement(WebPagePlacement placement) {        
        return this.dao.getAllDirectoriesInPlacement(placement);
    }
    
    @Override
    public List<WebPage> getAllWebPagesInPlacement(WebPagePlacement placement) {        
        return this.dao.getAllWebPagesInPlacement(placement);
    }   
    
    @Override
    public List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement, boolean strict) {
        
        directory = directory.trim().toLowerCase();
        if ( ! directory.matches(WEB_NAME_REGEXP) ) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return Collections.emptyList();
        }
        return this.dao.getAllWebPagesInDirectoryAndPlacement(
                directory, placement, strict);
    }
    
    @Override
    public List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPagePlacement place) {
        
        if ( ! dir.matches(WEB_NAME_REGEXP) || ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Page or directory name is invalid.");
            return Collections.emptyList();
        }
        return this.dao.getWebPagesByNameInDirAndPlace(name, dir, place);
    }
    
    @Override
    public List<WebPage> getWebPages(String name) {
        name = name.trim().toLowerCase();
        if ( ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Page name is invalid.");
            return Collections.emptyList();
        }
        if (name.contains("-")){
            return this.dao.getWebPagesByNameParts(name.split("-"));
        } else {
            return this.dao.getWebPagesByName(name);
        }
    }
    
    @Override
    public boolean editWebPageName(String name, String newName) {        
        name = name.trim().toLowerCase();
        newName = newName.trim().toLowerCase();
        if ( ! newName.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("New page name is invalid.");
            return false;
        }
        if ( newName.endsWith("/") ) {
            newName = newName.substring(0, name.length()-1);
        }
        return this.dao.editWebPageName(name, newName);
    }
    
    @Override
    public boolean editWebPageShortcuts(String name, String newShorts) {        
        name = name.trim().toLowerCase();
        newShorts = newShorts.trim().toLowerCase();
        return this.dao.editWebPageShortcuts(name, newShorts);
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl) {
        name = name.trim().toLowerCase();
        return this.dao.editWebPageUrl(name, newUrl);
    }
        
    @Override
    public boolean editWebPageBrowser(String name, String newBrowser) {
        
        name = name.trim().toLowerCase();
        newBrowser = newBrowser.trim().toLowerCase();
        return this.dao.editWebPageBrowser(name, newBrowser);
    }
    
    @Override
    public boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder) {
        if ( newOrder <= 0 ) {
            return false;
        }
        if ( ! dir.matches(WEB_NAME_REGEXP) || ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Page or directory name is invalid.");
            return false;
        }
        return this.dao.editWebPageOrder(name, dir, place, newOrder);
    }
    
    @Override
    public WebPageDirectory getDirectoryExact(WebPagePlacement place, String dir) {
        dir = dir.trim().toLowerCase();
        if ( ! dir.matches(WEB_NAME_REGEXP) ) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return null;
        } 
        return this.dao.getDirectoryExact(place, dir);
    }
    
    @Override
    public List<WebPageDirectory> getAllDirectoriesIn(WebPagePlacement placement) {
        return this.dao.getAllDirectoriesIn(placement);
    }
    
    @Override
    public boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPagePlacement placement) {
        
        directory = directory.trim().toLowerCase();
        newDirectory = newDirectory.trim().toLowerCase();
        if ( ! newDirectory.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("New directory name is invalid.");
            return false;
        }
        return this.dao.renameDirectoryInPlacement(directory, newDirectory, placement);
    }
    
    @Override
    public boolean editDirectoryOrder(
            WebPagePlacement place, String name, int newOrder) {
        
        if ( newOrder <= 0 ) {
            return false;
        }
        if ( ! name.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return false;
        }
        return this.dao.editDirectoryOrder(place, name, newOrder);
    }
    
    @Override
    public boolean deleteDirectoryAndPages(String dir, WebPagePlacement place) {
        dir = dir.trim().toLowerCase();
        if ( ! dir.matches(WEB_NAME_REGEXP) ) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return false;
        }        
        WebPageDirectory dirToDelete = new WebPageDirectory(dir, place);
        return this.dao.deleteDirectoryAndPages(dirToDelete);
    }
    
    @Override
    public boolean createEmptyDirectory(WebPagePlacement place, String dir) {
        dir = dir.trim().toLowerCase();
        if ( ! dir.matches(WEB_NAME_REGEXP) ) {
            this.ioEngine.reportMessage("Directory name is invalid.");
            return false;
        }
        return this.dao.createEmptyDirectory(place, dir);
    }
    
    @Override
    public boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPagePlacement oldPlacement, 
            String newDir, 
            WebPagePlacement newPlacement) {
                
        pageName = pageName.trim().toLowerCase();
        newDir = newDir.trim().toLowerCase();
        if ( ! newDir.matches(WEB_NAME_REGEXP) || ! pageName.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("New directory name is invalid.");
            return false;
        } 
        oldDir = oldDir.trim().toLowerCase();
        if ( ! oldDir.matches(WEB_NAME_REGEXP) || ! pageName.matches(WEB_NAME_REGEXP)) {
            this.ioEngine.reportMessage("Old directory name is invalid.");
            return false;
        }
        return this.dao.moveWebPageToPlacementAndDirectory(
                pageName, oldDir, oldPlacement, newDir, newPlacement);
    }
}
