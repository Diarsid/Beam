/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Program;

/**
 *
 * @author Diarsid
 */
public interface ProgramsKeeper extends NamedEntitiesKeeper {
    
    Optional<Program> findProgram(
            Initiator initiator, ArgumentsCommand command);
    
    @Override
    Optional<Program> findByExactName(
            Initiator initiator, String strictName);
    
    @Override
    Optional<Program> findByNamePattern(
            Initiator initiator, String pattern);
    
    List<Program> getProgramsByPattern(
            Initiator initiator, String pattern);
}
