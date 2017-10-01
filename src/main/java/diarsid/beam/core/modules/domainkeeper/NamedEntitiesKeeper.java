/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.base.control.flow.ValueFlow;

/**
 *
 * @author Diarsid
 */
public interface NamedEntitiesKeeper {
    
    ValueFlow<? extends NamedEntity> findByExactName(Initiator initiator, String name);
    
    ValueFlow<? extends NamedEntity> findByNamePattern(Initiator initiator, String pattern);
    
    boolean isSubjectedTo(InvocationCommand command);
    
}
