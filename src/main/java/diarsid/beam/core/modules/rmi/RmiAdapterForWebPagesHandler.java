/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlacement;
import diarsid.beam.core.modules.data.HandlerWebPages;
import diarsid.beam.core.rmi.interfaces.RmiWebPagesHandlerInterface;

/**
 *
 * @author Diarsid
 */
public class RmiAdapterForWebPagesHandler implements RmiWebPagesHandlerInterface {

    private final HandlerWebPages webHandler;
    
    public RmiAdapterForWebPagesHandler(HandlerWebPages handler) {
        this.webHandler = handler;
    }
    
    @Override
    public boolean newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPlacement placement, 
            String directory, 
            String browser) 
            throws RemoteException {
        
        return this.webHandler.saveWebPage(
                name, shortcuts, urlAddress, placement, directory, browser);
    }
    
    @Override
    public boolean deleteWebPage(String name, String dir, WebPlacement place) 
            throws RemoteException {
        
        return this.webHandler.deleteWebPage(name, dir, place);
    }
    
    @Override
    public List<String> getAllDirectoriesInPlacement(WebPlacement placement)
            throws RemoteException {
        
        return this.webHandler.getAllDirectoriesInPlacement(placement);
    }
    
    @Override
    public List<WebPage> getAllPagesInPlacement(WebPlacement placement) 
            throws RemoteException {
        
        return this.webHandler.getAllWebPagesInPlacement(placement);
    }   
    
    @Override
    public List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPlacement placement) throws RemoteException {
               
        // third parameter that is set to FALSE means that dao will not search
        // directory in strict mode:
        // - if FALSE dao will search any dirs containing given String 
        //   'directory' param;
        // - if TRUE dao will search dirs that matches given String 
        //   'directory' param exactly;
        // TRUE is used in web access to database in order to provide only
        // precise data through REST web API. Because this method will be used
        // mostly with external access program that needs more flexible and less
        // precise behavior, it is set to FALSE here. It allows user to get 
        // more data using less strict queries.
        return this.webHandler.getAllWebPagesInDirectoryAndPlacement(
                directory, placement, false);
    }
    
    @Override
    public List<WebPage> getWebPages(String name) throws RemoteException {
        return this.webHandler.getWebPages(name);
    }
    
    @Override
    public boolean editWebPageName(String name, String newName) 
            throws RemoteException {        
        
        return this.webHandler.editWebPageName(name, newName);
    }
    
    @Override
    public boolean editWebPageShortcuts(String name, String newShorts) 
            throws RemoteException {
        
        return this.webHandler.editWebPageShortcuts(name, newShorts);
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl) 
            throws RemoteException {
        
        return this.webHandler.editWebPageUrl(name, newUrl);
    }
        
    @Override
    public boolean editWebPageBrowser(String name, String newBrowser) 
            throws RemoteException {
                
        return this.webHandler.editWebPageBrowser(name, newBrowser);
    }
    
    @Override
    public boolean editWebPageOrder(
            String name, String dir, WebPlacement place, int newOrder) 
            throws RemoteException {
        
        return this.webHandler.editWebPageOrder(name, dir, place, newOrder);
    }
    
    @Override
    public boolean renameDirectory(
            String directory, String newDirectory, WebPlacement placement) 
            throws RemoteException {
                
        return this.webHandler.renameDirectoryInPlacement(
                directory, newDirectory, placement);
    }
    
    @Override
    public boolean deleteDirectory(String name, WebPlacement place) 
            throws RemoteException {
        
        return this.webHandler.deleteDirectoryAndPages(name, place);
    }
    
    @Override
    public boolean editDirectoryOrder(
            WebPlacement place, String name, int newOrder) 
            throws RemoteException {
        
        return this.webHandler.editDirectoryOrder(place, name, newOrder);
    }
    
    @Override
    public boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPlacement oldPlacement, 
            String newDir, 
            WebPlacement newPlacement) 
            throws RemoteException {        
               
        return this.webHandler.moveWebPageTo(
                pageName, oldDir, oldPlacement, newDir, newPlacement);
    }
}
