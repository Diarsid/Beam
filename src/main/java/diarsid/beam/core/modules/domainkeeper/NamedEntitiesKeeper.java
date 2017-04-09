/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;

/**
 *
 * @author Diarsid
 */
public interface NamedEntitiesKeeper {
    
    Optional<? extends NamedEntity> findByExactName(Initiator initiator, String name);
    
    Optional<? extends NamedEntity> findByNamePattern(Initiator initiator, String pattern);
    
    boolean isSubjectedTo(EntityInvocationCommand command);
    
}
