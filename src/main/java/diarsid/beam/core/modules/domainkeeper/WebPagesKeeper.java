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
import diarsid.beam.core.domain.entities.exceptions.DomainConsistencyException;

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
    Optional<WebPage> findByExactName(
            Initiator initiator, String name);
    
    @Override
    Optional<WebPage> findByNamePattern(
            Initiator initiator, String name);
    
    boolean createWebPage(
            Initiator initiator, String name, String url, WebPlace place, String directory)
            throws DomainConsistencyException;
    
    boolean editWebPageName(
            Initiator initiator, String name, String newName)
            throws DomainConsistencyException;
}
