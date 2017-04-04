/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenPathCommand;

/**
 *
 * @author Diarsid
 */
public interface CommandsMemoryKeeper {
    
    Optional<ExtendableCommand> findStoredCommandByExactOriginalOfAnyType(
            Initiator initiator, String original); 
    
    void tryToExtendCommand(
            Initiator initiator, InvocationEntityCommand command);
    
    void tryToExtendCommand(
            Initiator initiator, OpenPathCommand command);
    
    void save(
            Initiator initiator, ExtendableCommand command);
    
    void remove(
            Initiator initiator, ExtendableCommand command);
}
