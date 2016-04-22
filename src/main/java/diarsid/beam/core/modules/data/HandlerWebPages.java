/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;

/**
 *
 * @author Diarsid
 */
public interface HandlerWebPages {
    
    boolean saveWebPage(
            String name,
            String shortcuts, 
            String urlAddress, 
            WebPagePlacement placement, 
            String directory, 
            String browser);
        
    boolean deleteWebPage(String name, String dir, WebPagePlacement place);
    
    List<String> getAllDirectoriesInPlacement(WebPagePlacement placement);
    
    List<WebPageDirectory> getAllDirectoriesIn(WebPagePlacement placement);
    
    List<WebPage> getAllWebPagesInPlacement(WebPagePlacement placement);
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String directory, WebPagePlacement placement, boolean strict);
    
    List<WebPage> getWebPages(String name);
    
    List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPagePlacement place);
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageShortcuts(String name, String newShortcuts);
    
    boolean editWebPageUrl(String name, String newUrl);
        
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder);
    
    boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPagePlacement placement);
    
    boolean editDirectoryOrder(WebPagePlacement place, String name, int newOrder);
    
    boolean moveWebPageTo(
            String pageName, 
            String oldDir, 
            WebPagePlacement oldPlacement, 
            String newDir, 
            WebPagePlacement newPlacement);
    
    boolean createEmptyDirectory(WebPagePlacement place, String dirName);
    
    WebPageDirectory getDirectoryExact(WebPagePlacement place, String dir);
    
    boolean deleteDirectoryAndPages(String dir, WebPagePlacement place);
}
