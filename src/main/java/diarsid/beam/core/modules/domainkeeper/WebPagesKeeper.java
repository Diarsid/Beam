/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.flow.VoidOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlace;

/**
 *
 * @author Diarsid
 */
public interface WebPagesKeeper {
    
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
    
    Optional<WebPage> getWebPageByName(
            Initiator initiator, String name);
    
    boolean createWebPage(
            Initiator initiator, String name, String url, WebPlace place, String directory);
    
    boolean editWebPageName(
            Initiator initiator, String name, String newName);
}
