/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPagePlacement;

/**
 *
 * @author Diarsid
 */
public interface RmiWebPageHandlerInterface extends Remote {
    
    void newWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser) throws RemoteException;
        
    boolean deleteWebPage(String name) throws RemoteException;
    
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
    
    boolean renameDirectory(
            String directory, String newDirectory, WebPagePlacement placement) 
            throws RemoteException;
    
    boolean moveWebPageTo
            (String pageName, String newDirectory, WebPagePlacement placement)
            throws RemoteException;
}
