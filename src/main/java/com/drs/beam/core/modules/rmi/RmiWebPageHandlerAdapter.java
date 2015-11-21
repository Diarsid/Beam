/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.rmi;

import java.rmi.RemoteException;
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.modules.data.DaoWebPages;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;

/**
 *
 * @author Diarsid
 */
public class RmiWebPageHandlerAdapter implements RmiWebPageHandlerInterface{
    // Fields =============================================================================

    private final DaoWebPages dao;
    
    // Constructors =======================================================================
    public RmiWebPageHandlerAdapter(DaoWebPages dao) {
        this.dao = dao;
    }
    // Methods ============================================================================
    
    @Override
    public void newWebPage(String name, String urlAddress, String category, String browser) throws RemoteException {
        name = name.trim().toLowerCase();
        urlAddress = urlAddress.trim().toLowerCase();
        category = category.trim().toLowerCase();
        browser = browser.trim().toLowerCase();
        this.dao.saveWebPage(new WebPage(name, urlAddress, category, browser));
    }
    
    @Override
    public boolean deleteWebPage(String name) throws RemoteException {
        name = name.trim().toLowerCase();
        return this.dao.deleteWebPage(name);
    }
    
    @Override
    public List<String> getAllCategories() throws RemoteException {
        return this.dao.getAllCategories();
    }
    
    @Override
    public List<WebPage> getAllPages() throws RemoteException {
        return this.dao.getAllWebPages();
    }   
    
    @Override
    public List<WebPage> getAllWebPagesOfCategory(String category) throws RemoteException {
        category = category.trim().toLowerCase();
        return this.dao.getAllWebPagesOfCategory(category);
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
    public boolean editWebPageName(String name, String newName) throws RemoteException {
        name = name.trim().toLowerCase();
        newName = newName.trim().toLowerCase();
        return this.dao.editWebPageName(name, newName);
    }
    
    @Override
    public boolean editWebPageUrl(String name, String newUrl) throws RemoteException {
        name = name.trim().toLowerCase();
        newUrl = newUrl.trim().toLowerCase();
        return this.dao.editWebPageUrl(name, newUrl);
    }
    
    @Override
    public boolean editWebPageCategory(String name, String newCategory) throws RemoteException {
        name = name.trim().toLowerCase();
        newCategory = newCategory.trim().toLowerCase();
        return this.dao.editWebPageCategory(name, newCategory);
    }
    
    @Override
    public boolean editWebPageBrowser(String name, String newBrowser) throws RemoteException {
        name = name.trim().toLowerCase();
        newBrowser = newBrowser.trim().toLowerCase();
        return this.dao.editWebPageBrowser(name, newBrowser);
    }
    
    @Override
    public boolean renameCategory(String category, String newCategory) throws RemoteException {
        category = category.trim().toLowerCase();
        newCategory = newCategory.trim().toLowerCase();
        return this.dao.renameCategory(category, newCategory);
    }
}
