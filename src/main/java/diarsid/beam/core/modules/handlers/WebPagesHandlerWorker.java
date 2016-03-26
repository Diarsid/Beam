/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.handlers;

import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoWebPages;

import static diarsid.beam.core.entities.WebPage.WEB_NAME_REGEXP;

/**
 *
 * @author Diarsid
 */
class WebPagesHandlerWorker implements WebPagesHandler {
    
    private final DaoWebPages dao;
    private final IoInnerModule ioEngine;
    
    WebPagesHandlerWorker(IoInnerModule io, DaoWebPages dao) {
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
            return false;
        }
        if ( name.endsWith("/") ) {
            name = name.substring(0, name.length()-1);
        }
        urlAddress = urlAddress.trim().toLowerCase();
        directory = directory.trim().toLowerCase();
        if ( ! directory.matches(WEB_NAME_REGEXP)) {
            return false;
        }
        browser = browser.trim().toLowerCase();
        return this.dao.saveWebPage(WebPage.newPage(
                name, shortcuts, urlAddress, placement, directory, browser));
    }
    
    @Override
    public boolean deleteWebPage(String name, String dir, WebPagePlacement place) {        
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
        return this.dao.getAllWebPagesInDirectoryAndPlacement(
                directory, placement, strict);
    }
    
    @Override
    public List<WebPage> getWebPages(String name) {
        name = name.trim().toLowerCase();
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
        newUrl = newUrl.trim().toLowerCase();
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
        return this.dao.editWebPageOrder(name, dir, place, newOrder);
    }
    
    @Override
    public boolean renameDirectory(
            String directory, String newDirectory, WebPagePlacement placement) {
        
        directory = directory.trim().toLowerCase();
        newDirectory = newDirectory.trim().toLowerCase();
        if ( ! newDirectory.matches(WEB_NAME_REGEXP)) {
            return false;
        }
        return this.dao.editDirectoryNameInPlacement(directory, newDirectory, placement);
    }
    
    @Override
    public boolean editDirectoryOrder(
            WebPagePlacement place, String name, int newOrder) {
        
        return this.dao.editDirectoryOrder(place, name, newOrder);
    }
    
    @Override
    public boolean moveWebPageTo
            (String pageName, String newDirectory, WebPagePlacement placement) {
        
        if ( ! newDirectory.matches(WEB_NAME_REGEXP)) {
            return false;
        }        
        return this.dao.moveWebPageToPlacementAndDirectory
                (pageName, newDirectory, placement);
    }
}
