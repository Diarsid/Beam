/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.data.DaoWebPages;
import diarsid.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;

/**
 *
 * @author Diarsid
 */
public class RmiAdapterForWebPageHandler implements RmiWebPageHandlerInterface {

    private final DaoWebPages dao;
    
    public RmiAdapterForWebPageHandler(DaoWebPages dao) {
        this.dao = dao;
    }
    
    @Override
    public boolean newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        if ( ! name.matches("[a-zA-Z0-9-_>\\s]+")) {
            return false;
        }
        urlAddress = urlAddress.trim().toLowerCase();
        directory = directory.trim().toLowerCase();
        if ( ! directory.matches("[a-zA-Z0-9-_>\\s]+")) {
            return false;
        }
        browser = browser.trim().toLowerCase();
        return this.dao.saveWebPage(WebPage.newPage(
                name, shortcuts, urlAddress, placement, directory, browser));
    }
    
    @Override
    public boolean deleteWebPage(String name, String dir, WebPagePlacement place) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        return this.dao.deleteWebPage(name, dir, place);
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
        if ( ! newName.matches("[a-zA-Z0-9-_>\\s]+")) {
            return false;
        }
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
    public boolean editWebPageBrowser(String name, String newBrowser) 
            throws RemoteException {
        
        name = name.trim().toLowerCase();
        newBrowser = newBrowser.trim().toLowerCase();
        return this.dao.editWebPageBrowser(name, newBrowser);
    }
    
    @Override
    public boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder) 
            throws RemoteException {
        return this.dao.editWebPageOrder(name, dir, place, newOrder);
    }
    
    @Override
    public boolean renameDirectory(
            String directory, String newDirectory, WebPagePlacement placement) 
            throws RemoteException {
        
        directory = directory.trim().toLowerCase();
        newDirectory = newDirectory.trim().toLowerCase();
        if ( ! newDirectory.matches("[a-zA-Z0-9-_>\\s]+")) {
            return false;
        }
        return this.dao.editDirectoryNameInPlacement(directory, newDirectory, placement);
    }
    
    @Override
    public boolean editDirectoryOrder(WebPagePlacement place, String name, int newOrder) {
        return this.dao.editDirectoryOrder(place, name, newOrder);
    }
    
    @Override
    public boolean moveWebPageTo
            (String pageName, String newDirectory, WebPagePlacement placement)
            throws RemoteException {
        
        if ( ! newDirectory.matches("[a-zA-Z0-9-_>\\s]+")) {
            return false;
        }        
        return this.dao.moveWebPageToPlacementAndDirectory
                (pageName, newDirectory, placement);
    }
}
