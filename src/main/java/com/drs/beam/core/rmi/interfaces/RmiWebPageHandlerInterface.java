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

/**
 *
 * @author Diarsid
 */
public interface RmiWebPageHandlerInterface extends Remote {
    
    void newWebPage(String name, String urlAddress, String category, String browser) throws RemoteException;
        
    boolean deleteWebPage(String name) throws RemoteException;
    
    List<String> getAllCategories() throws RemoteException;
    
    List<WebPage> getAllPages() throws RemoteException;   
    
    List<WebPage> getAllWebPagesOfCategory(String category) throws RemoteException;
    
    List<WebPage> getWebPages(String name) throws RemoteException;
    
    boolean editWebPageName(String name, String newName) throws RemoteException;
    
    boolean editWebPageUrl(String name, String newUrl) throws RemoteException;
    
    boolean editWebPageCategory(String name, String newCategory) throws RemoteException;
    
    boolean editWebPageBrowser(String name, String newBrowser) throws RemoteException;
    
    boolean renameCategory(String category, String newCategory) throws RemoteException;
}
