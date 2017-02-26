/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface DaoWebPages {
    
    Optional<Integer> freeNameNextIndex(
            Initiator initiator, String name);
    
    Optional<WebPage> getByName(
            Initiator initiator, String name);
    
    List<WebPage> findByPattern(
            Initiator initiator, String pattern);
    
    List<WebPage> allFromDirectory(
            Initiator initiator, String dirName, WebPlace place);
    
    boolean save(
            Initiator initiator, WebPage page);
    
    boolean remove(
            Initiator initiator, String name);
    
    boolean editName(
            Initiator initiator, String oldName, String newName);
    
    boolean editShortcuts(
            Initiator initiator, String name, String newShortcuts);
    
    boolean editUrl(
            Initiator initiator, String name, String newUrl);    
    
    boolean movePageFromDirToDir(
            Initiator initiator, 
            String pageName, 
            String oldDirName, 
            WebPlace oldPlace, 
            String newDirName, 
            WebPlace newPlace);
    
    boolean updatePageOrdersInDir(
            Initiator initiator, List<WebPage> pages);
}
