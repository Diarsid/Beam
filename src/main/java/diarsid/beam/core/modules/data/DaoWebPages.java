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
    
    boolean saveWebPage(WebPage page);
    
    boolean deleteWebPage(String name, String dir, WebPagePlacement placement);
    
    List<WebPage> getAllWebPagesInPlacement(WebPagePlacement placement);
    
    List<WebPage> getWebPagesByName(String name);
    
    List<WebPage> getWebPagesByNameParts(String[] nameParts);
    
    List<WebPage> getWebPagesByNameInDirAndPlace(
            String name, String dir, WebPagePlacement place);
    
    List<WebPage> getAllWebPagesInDirectoryAndPlacement(
            String dir, WebPagePlacement placement, boolean dirStrictMatch);
    
    List<String> getAllDirectoriesInPlacement(WebPagePlacement placement);    
    
    boolean editWebPageName(String name, String newName);
    
    boolean editWebPageShortcuts(String name, String newShortcuts);
    
    boolean editWebPageUrl(String name, String newUrl);
    
    boolean editWebPageBrowser(String name, String newBrowser);
    
    boolean editWebPageOrder(
            String name, String dir, WebPagePlacement place, int newOrder);
    
    boolean renameDirectoryInPlacement(
            String directory, String newDirectory, WebPagePlacement placement);
    
    boolean moveWebPageToPlacementAndDirectory(
            String pageName, String newDirectory, WebPagePlacement placement);
    
    boolean deleteDirectoryAndPages(WebPageDirectory dir);
    
    boolean createEmptyDirectory(
            WebPagePlacement place, String name);
    
    WebPageDirectory getDirectoryExact(WebPagePlacement place, String name);
    
    List<WebPageDirectory> getAllDirectoriesIn(WebPagePlacement place);
    
    boolean editDirectoryOrder(WebPagePlacement place, String name, int newOrder);
}
