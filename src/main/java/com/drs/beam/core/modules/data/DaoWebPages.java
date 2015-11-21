/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.sql.SQLException;
import java.util.List;

import com.drs.beam.core.entities.WebPage;

/**
 *
 * @author Diarsid
 */
public interface DaoWebPages {
    
    void saveWebPage(WebPage page);
    
    boolean deleteWebPage(String name);
    
    List<WebPage> getAllWebPages();
    
    List<WebPage> getWebPagesByName(String name);
    List<WebPage> getWebPagesByNameParts(String[] nameParts);
    
    List<WebPage> getAllWebPagesOfCategory(String category);
    
    List<String> getAllCategories();    
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageUrl(String name, String newUrl);
    
    boolean editWebPageCategory(String name, String newCategory);
    
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean renameCategory(String category, String newCategory);
}
