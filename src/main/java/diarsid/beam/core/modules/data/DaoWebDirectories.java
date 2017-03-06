/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebDirectoryPages;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface DaoWebDirectories {
    
    Optional<Integer> freeNameNextIndex(
            Initiator initiator, String name, WebPlace place);
    
    List<WebDirectoryPages> getAllDirectoriesPages(
            Initiator initiator);
    
    List<WebDirectoryPages> getAllDirectoriesPagesInPlace(
            Initiator initiator, WebPlace place);
    
    Optional<WebDirectoryPages> getDirectoryPagesByNameInPlace(
            Initiator initiator, String name, WebPlace place);
    
    Optional<WebDirectory> getDirectoryByNameInPlace(
            Initiator initiator, String name, WebPlace place);
    
    List<WebDirectory> findDirectoriesByPatternInPlace(
            Initiator initiator, String pattern, WebPlace place);
    
    List<WebDirectory> findDirectoriesByPatternInAnyPlace(
            Initiator initiator, String pattern);
    
    List<WebDirectory> getAllDirectories(
            Initiator initiator);
    
    List<WebDirectory> getAllDirectoriesInPlace(
            Initiator initiator, WebPlace place);
    
    boolean exists(
            Initiator initiator, String directoryName, WebPlace place);
    
    boolean updateWebDirectoryOrders(
            Initiator initiator, List<WebDirectory> directories);
        
    boolean save(
            Initiator initiator, WebDirectory directory);

    boolean remove(
            Initiator initiator, String name, WebPlace place);
    
    boolean moveDirectoryToPlace(
            Initiator initiator, String name, WebPlace from, WebPlace to);
    
    boolean editDirectoryName(
            Initiator initiator, String name, WebPlace place, String newName);
}
