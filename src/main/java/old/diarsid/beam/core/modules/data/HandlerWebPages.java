/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPlacement;

/**
 *
 * @author Diarsid
 */
public interface HandlerWebPages {
    
    boolean saveWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPlacement placement, 
            String directory, 
            String browser);
        
    boolean deleteWebPage(String name, String dir, WebPlacement place);
    
    List<String> getAllDirectoriesInPlacement(WebPlacement placement);
    
    List<WebDirectory> getAllDirectoriesIn(WebPlacement placement);
    
    List<WebPage> getAllWebPagesInPlacement(WebPlacement placement);
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPlacement placement, boolean strict);
    
    List<WebPage> getWebPages(String name);
    
    List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPlacement place);
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageShortcuts(String name, String newShortcuts);
    
    boolean editWebPageUrl(String name, String newUrl);
        
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean editWebPageOrder(
            String name, String dir, WebPlacement place, int newOrder);
    
    boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPlacement placement);
    
    boolean editDirectoryOrder(WebPlacement place, String name, int newOrder);
    
    boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPlacement oldPlacement, 
            String newDir, 
            WebPlacement newPlacement);
    
    boolean createEmptyDirectory(WebPlacement place, String dirName);
    
    WebDirectory getDirectoryExact(WebPlacement place, String dir);
    
    boolean deleteDirectoryAndPages(String dir, WebPlacement place);
}
