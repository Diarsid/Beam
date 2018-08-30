/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;

/**
 *
 * @author Diarsid
 */
public interface NamedEntitiesKeeper <T extends NamedEntity> {
    
    ValueFlow<T> findByExactName(Initiator initiator, String name);
    
    ValueFlow<T> findByNamePattern(Initiator initiator, String pattern);
    
    ValueFlow<Message> findAll(Initiator initiator);
    
    boolean isSubjectedTo(InvocationCommand command);
    
}
