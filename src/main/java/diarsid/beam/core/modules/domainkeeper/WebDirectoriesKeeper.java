/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;


import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebDirectory;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface WebDirectoriesKeeper {
    
    VoidOperation createWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation deleteWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    ValueOperation<? extends WebDirectory> findWebDirectory(
            Initiator initiator, ArgumentsCommand command);
    
    WebResponse createWebDirectory(WebPlace place, String name);
    
    WebResponse deleteWebDirectory(WebPlace place, String name);
    
    WebResponse editWebDirectoryName(WebPlace place, String name, String newName);
    
    WebResponse editWebDirectoryPlace(WebPlace place, String name, WebPlace newPlace);
    
    WebResponse editWebDirectoryOrder(WebPlace place, String name, int newOrder);
    
    WebResponse getWebDirectory(WebPlace place, String name);
    
    WebResponse getWebDirectoryPages(WebPlace place, String name);
    
    WebResponse getAllDirectoriesInPlace(WebPlace place);
}
