/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPagePlacement;

/**
 *
 * @author Diarsid
 */
public interface RmiWebPagesHandlerInterface extends Remote {
    
    boolean newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser) throws RemoteException;
        
    boolean deleteWebPage(String name, String dir, WebPagePlacement place) 
            throws RemoteException;
    
    List<String> getAllDirectoriesInPlacement(WebPagePlacement placement) 
            throws RemoteException;
    
    List<WebPage> getAllPagesInPlacement(WebPagePlacement placement) 
            throws RemoteException;   
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement) 
            throws RemoteException;
    
    List<WebPage> getWebPages(String name) throws RemoteException;
    
    boolean editWebPageName(String name, String newName) throws RemoteException;
    
    boolean editWebPageShortcuts(String name, String newShortcuts) 
            throws RemoteException;
    
    boolean editWebPageUrl(String name, String newUrl) throws RemoteException;
        
    boolean editWebPageBrowser(String name, String newBrowser) 
            throws RemoteException;
    
    boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder) 
            throws RemoteException;
    
    boolean renameDirectory(
            String directory, String newDirectory, WebPagePlacement placement) 
            throws RemoteException;
    
    boolean deleteDirectory(
            String directory, WebPagePlacement placement) throws RemoteException;
    
    boolean editDirectoryOrder(WebPagePlacement place, String name, int newOrder)
            throws RemoteException;
    
    boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPagePlacement oldPlacement, 
            String newDir, 
            WebPagePlacement newPlacement) 
            throws RemoteException;
}
