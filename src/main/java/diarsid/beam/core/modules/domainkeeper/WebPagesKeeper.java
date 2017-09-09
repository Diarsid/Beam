/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface WebPagesKeeper extends NamedEntitiesKeeper {
    
    VoidOperation createWebPage(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation editWebPage(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation removeWebPage(
            Initiator initiator, ArgumentsCommand command);
    
    VoidOperation captureImage(
            Initiator initiator, ArgumentsCommand command);
    
    ValueOperation<WebPage> findWebPageByPattern(
            Initiator initiator, ArgumentsCommand command);
    
    List<WebPage> findWebPagesByPattern(
            Initiator initiator, String pattern);
    
    ValueOperation<Message> getWebPlace(
            Initiator initiator, EmptyCommand command);
    
    @Override
    ValueOperation<WebPage> findByExactName(
            Initiator initiator, String name);
    
    @Override
    ValueOperation<WebPage> findByNamePattern(
            Initiator initiator, String name);
    
    WebResponse createWebPage(
            WebPlace place, String directoryName, String pageName, String url);
    
    WebResponse getWebPage(
            WebPlace place, String directoryName, String pageName);
    
    WebResponse getWebPagesInDirectory(
            WebPlace place, String directoryName);
    
    WebResponse getWebPageImage(
            WebPlace place, String directoryName, String pageName);
    
    WebResponse deleteWebPage(
            WebPlace place, String directoryName, String pageName);
    
    WebResponse editWebPageName(
            WebPlace place, String directoryName, String pageName, String pageNewName);
    
    WebResponse editWebPageUrl(
            WebPlace place, String directoryName, String pageName, String pageUrl);
    
    WebResponse editWebPageDirectory(
            WebPlace place, String directoryName, String pageName, String newDirectoryName);
    
    WebResponse editWebPageDirectoryAndPlace(
            WebPlace place, 
            String directoryName, 
            String pageName, 
            WebPlace newPlace, 
            String newDirectoryName);
    
    WebResponse editWebPageOrder(
            WebPlace place, String directoryName, String pageName, int newOrder);
}
