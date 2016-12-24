/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlacement;

/**
 *
 * @author Diarsid
 */
public interface RmiWebPagesHandlerInterface extends Remote {
    
    boolean newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPlacement placement, 
            String directory, 
            String browser) throws RemoteException;
        
    boolean deleteWebPage(String name, String dir, WebPlacement place) 
            throws RemoteException;
    
    List<String> getAllDirectoriesInPlacement(WebPlacement placement) 
            throws RemoteException;
    
    List<WebPage> getAllPagesInPlacement(WebPlacement placement) 
            throws RemoteException;   
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPlacement placement) 
            throws RemoteException;
    
    List<WebPage> getWebPages(String name) throws RemoteException;
    
    boolean editWebPageName(String name, String newName) throws RemoteException;
    
    boolean editWebPageShortcuts(String name, String newShortcuts) 
            throws RemoteException;
    
    boolean editWebPageUrl(String name, String newUrl) throws RemoteException;
        
    boolean editWebPageBrowser(String name, String newBrowser) 
            throws RemoteException;
    
    boolean editWebPageOrder(
            String name, String dir, WebPlacement place, int newOrder) 
            throws RemoteException;
    
    boolean renameDirectory(
            String directory, String newDirectory, WebPlacement placement) 
            throws RemoteException;
    
    boolean deleteDirectory(
            String directory, WebPlacement placement) throws RemoteException;
    
    boolean editDirectoryOrder(WebPlacement place, String name, int newOrder)
            throws RemoteException;
    
    boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPlacement oldPlacement, 
            String newDir, 
            WebPlacement newPlacement) 
            throws RemoteException;
}
