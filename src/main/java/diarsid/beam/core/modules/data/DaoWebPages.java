/*
 * project: Beam
 * author: Diarsid
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
    
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPagePlacement placement);
    
    boolean moveWebPageToPlacementAndDirectory(
            String pageName, String newDirectory, WebPagePlacement placement);
    
    boolean deleteDirectoryAndPages(WebPageDirectory dir);
    
    boolean createEmptyDirectoryWithDefaultOrder(
            WebPagePlacement place, String name);
    
    WebPageDirectory getDirectoryExact(WebPagePlacement place, String name);
    
    List<WebPageDirectory> getAllDirectoriesIn(WebPagePlacement place);
    
    boolean changeDirectoryOrder(WebPagePlacement place, String name, int newOrder);
}