/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebPage;

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
    
    ValueOperation<WebPage> findWebPageByPattern(
            Initiator initiator, ArgumentsCommand command);
    
    List<WebPage> findWebPagesByPattern(
            Initiator initiator, String pattern);
    
    @Override
    ValueOperation<WebPage> findByExactName(
            Initiator initiator, String name);
    
    @Override
    ValueOperation<WebPage> findByNamePattern(
            Initiator initiator, String name);
    
    void createWebPage(
            WebRequest webRequest) throws IOException;
    
    void getWebPagesInDirectory(
            WebRequest webRequest) throws IOException;
    
    void deleteWebPage(
            WebRequest webRequest) throws IOException;
    
    void editWebPageName(
            WebRequest webRequest) throws IOException;
    
    void editWebPageUrl(
            WebRequest webRequest) throws IOException;
    
    void editWebPageDirectory(
            WebRequest webRequest) throws IOException;
    
    void editWebPageDirectoryAndPlace(
            WebRequest webRequest) throws IOException;
    
    void editWebPageOrder(
            WebRequest webRequest) throws IOException;
}
