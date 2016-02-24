/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPagePlacement;
import com.drs.beam.core.modules.data.DaoWebPages;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;

/**
 *
 * @author Diarsid
 */
public class RmiAdapterForWebPageHandler implements RmiWebPageHandlerInterface{
    // Fields =============================================================================

    private final DaoWebPages dao;
    
    // Constructors =======================================================================
    public RmiAdapterForWebPageHandler(DaoWebPages dao) {
        this.dao = dao;
    }
    // Methods ============================================================================
    
    @Override
    public void newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        urlAddress = urlAddress.trim().toLowerCase();
        directory = directory.trim().toLowerCase();
        browser = browser.trim().toLowerCase();
        this.dao.saveWebPage(new WebPage(
                name, shortcuts, urlAddress, placement, directory, browser));
    }
    
    @Override
    public boolean deleteWebPage(String name) throws RemoteException {
        name = name.trim().toLowerCase();
        return this.dao.deleteWebPage(name);
    }
    
    @Override
    public List<String> getAllDirectoriesInPlacement(WebPagePlacement placement)
            throws RemoteException {
        
        return this.dao.getAllDirectoriesInPlacement(placement);
    }
    
    @Override
    public List<WebPage> getAllPagesInPlacement(WebPagePlacement placement) 
            throws RemoteException {
        
        return this.dao.getAllWebPagesInPlacement(placement);
    }   
    
    @Override
    public List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement) throws RemoteException {
        
        directory = directory.trim().toLowerCase();
        return this.dao.getAllWebPagesInDirectoryAndPlacement(directory, placement);
    }
    
    @Override
    public List<WebPage> getWebPages(String name) throws RemoteException {
        name = name.trim().toLowerCase();
        if (name.contains("-")){
            return this.dao.getWebPagesByNameParts(name.split("-"));
        } else {
            return this.dao.getWebPagesByName(name);
        }
    }
    
    @Override
    public boolean editWebPageName(String name, String newName) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        newName = newName.trim().toLowerCase();
        return this.dao.editWebPageName(name, newName);
    }
    
    @Override
    public boolean editWebPageShortcuts(String name, String newShorts) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        newShorts = newShorts.trim().toLowerCase();
        return this.dao.editWebPageShortcuts(name, newShorts);
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl) throws RemoteException {
        name = name.trim().toLowerCase();
        newUrl = newUrl.trim().toLowerCase();
        return this.dao.editWebPageUrl(name, newUrl);
    }
        
    @Override
    public boolean editWebPageBrowser(String name, String newBrowser) throws RemoteException {
        name = name.trim().toLowerCase();
        newBrowser = newBrowser.trim().toLowerCase();
        return this.dao.editWebPageBrowser(name, newBrowser);
    }
    
    @Override
    public boolean renameDirectory(
            String directory, String newDirectory, WebPagePlacement placement) 
            throws RemoteException {
        
        directory = directory.trim().toLowerCase();
        newDirectory = newDirectory.trim().toLowerCase();
        return this.dao.renameDirectoryInPlacement(directory, newDirectory, placement);
    }
    
    @Override
    public boolean moveWebPageTo
            (String pageName, String newDirectory, WebPagePlacement placement)
            throws RemoteException {
                
        return this.dao.moveWebPageToPlacementAndDirectory
                (pageName, newDirectory, placement);
    }
}
