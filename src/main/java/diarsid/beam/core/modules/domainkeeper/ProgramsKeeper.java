/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.FindEntityCommand;
import diarsid.beam.core.domain.entities.Program;

/**
 *
 * @author Diarsid
 */
public interface ProgramsKeeper {
    
    Optional<Program> findProgram(
            Initiator initiator, FindEntityCommand command);
    
    Optional<Program> getOneProgramByStrictName(
            Initiator initiator, String strictName);
    
    Optional<Program> getOneProgramByPattern(
            Initiator initiator, String pattern);
    
    List<Program> getProgramsByPattern(
            Initiator initiator, String pattern);
}
