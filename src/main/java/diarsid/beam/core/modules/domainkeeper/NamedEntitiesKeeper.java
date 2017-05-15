/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;

/**
 *
 * @author Diarsid
 */
public interface NamedEntitiesKeeper {
    
    ValueOperation<? extends NamedEntity> findByExactName(Initiator initiator, String name);
    
    ValueOperation<? extends NamedEntity> findByNamePattern(Initiator initiator, String pattern);
    
    boolean isSubjectedTo(InvocationCommand command);
    
}
