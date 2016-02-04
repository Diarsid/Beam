/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.data;

import java.sql.SQLException;
import java.util.List;

import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPagePlacement;

/**
 *
 * @author Diarsid
 */
public interface DaoWebPages {
    
    void saveWebPage(WebPage page);
    
    boolean deleteWebPage(String name);
    
    List<WebPage> getAllWebPagesInPlacement(WebPagePlacement placement);
    
    List<WebPage> getWebPagesByName(String name);
    List<WebPage> getWebPagesByNameParts(String[] nameParts);
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement);
    
    List<String> getAllDirectoriesInPlacement(WebPagePlacement placement);    
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageShortcuts(String name, String newShortcuts);
    
    boolean editWebPageUrl(String name, String newUrl);
    
    boolean editWebPageDirectory(String name, String newCategory);
    
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean renameDirectoryInPlacement(
            String category, String newCategory, WebPagePlacement placement);
    
    boolean moveWebPageToPlacementAndDirectory(
            String pageName, String newDirectory, WebPagePlacement placement);
}
