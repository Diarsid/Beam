/*
 * project: Beam
 * author: Diarsid
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
public interface DaoWebPages {
    
    boolean saveWebPage(WebPage page);
    
    boolean deleteWebPage(String name, String dir, WebPlacement placement);
    
    List<WebPage> getAllWebPagesInPlacement(WebPlacement placement);
    
    List<WebPage> getWebPagesByName(String name);
    
    List<WebPage> getWebPagesByNameParts(List<String> nameParts);
    
    List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPlacement place);
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String dir, WebPlacement placement, boolean dirStrictMatch);
    
    List<String> getAllDirectoriesInPlacement(WebPlacement placement);    
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageShortcuts(String name, String newShortcuts);
    
    boolean editWebPageUrl(String name, String newUrl);
    
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean editWebPageOrder(
            String name, String dir, WebPlacement place, int newOrder);
    
    boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPlacement placement);
    
    boolean moveWebPageToPlacementAndDirectory(
            String pageName, 
            String oldDirectory, 
            WebPlacement oldPlacement, 
            String newDirectory, 
            WebPlacement newPlacement);
    
    boolean deleteDirectoryAndPages(WebDirectory dir);
    
    boolean createEmptyDirectory(
            WebPlacement place, String name);
    
    WebDirectory getDirectoryExact(WebPlacement place, String name);
    
    List<WebDirectory> getAllDirectoriesIn(WebPlacement place);
    
    boolean editDirectoryOrder(WebPlacement place, String name, int newOrder);
}
