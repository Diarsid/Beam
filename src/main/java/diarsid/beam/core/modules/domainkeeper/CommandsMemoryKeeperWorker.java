/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.modules.data.DaoCommands;

import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
class CommandsMemoryKeeperWorker implements CommandsMemoryKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoCommands daoCommands;

    CommandsMemoryKeeperWorker(DaoCommands daoCommands, InnerIoEngine ioEngine) {
        this.daoCommands = daoCommands;
        this.ioEngine = ioEngine;
    }

    @Override
    public void tryToExtendCommand(Initiator initiator, InvocationCommand command) {
        Optional<InvocationCommand> exactMatch = this.extendingByExactMatch(initiator, command);
        if ( exactMatch.isPresent() ) {
            command.setStored();
            command.argument().setExtended(exactMatch.get().extendedArgument());
        } else {
            command.setNew();
            this.extendingByPattern(initiator, command);
        }
    }   

    public Optional<InvocationCommand> extendingByExactMatch(
            Initiator initiator, InvocationCommand command) {
        Optional<InvocationCommand> exactMatch = 
                this.daoCommands.getByExactOriginalAndType(
                        initiator, command.argument().original(), command.type());
        return exactMatch;
    }    
    
    private void extendingByPattern(Initiator initiator, InvocationCommand command) {
        List<InvocationCommand> foundInOriginal = 
                this.daoCommands.searchInOriginalByPatternAndType(
                        initiator, command.argument().original(), command.type());
        if ( nonEmpty(foundInOriginal) ) {
            this.chooseOneCommandAndUseAsExtension(initiator, foundInOriginal, command);
        } else {
            List<InvocationCommand> foundInExtended = 
                    this.daoCommands.searchInExtendedByPatternAndType(
                            initiator, command.argument().original(), command.type());
            if ( nonEmpty(foundInExtended) ) {
                this.chooseOneCommandAndUseAsExtension(initiator, foundInExtended, command);
            }
        }
    }

    private void chooseOneCommandAndUseAsExtension(
            Initiator initiator, 
            List<InvocationCommand> foundCommands, 
            InvocationCommand command) {
        if ( hasOne(foundCommands) ) {
            command.argument().setExtended(getOne(foundCommands).extendedArgument());
        } else {
            Optional<InvocationCommand> chosenCommand = 
                    this.chooseOneCommand(initiator, command, foundCommands);
            if ( chosenCommand.isPresent() ) {
                command.argument().setExtended(chosenCommand.get().extendedArgument());
            }
        }        
    }

    @Override
    public void save(Initiator initiator, InvocationCommand command) {
        this.daoCommands.save(initiator, command);
    }

    @Override
    public void remove(Initiator initiator, InvocationCommand command) {
        this.daoCommands.delete(initiator, command);
    }

    @Override
    public Optional<InvocationCommand> findStoredCommandByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        List<InvocationCommand> foundCommands = 
                this.daoCommands.getByExactOriginalOfAnyType(initiator, original);
        if ( nonEmpty(foundCommands) ) {
            if ( hasOne(foundCommands) ) {
                return Optional.of(getOne(foundCommands));
            } else {
                return this.chooseOneCommand(initiator, original, foundCommands);
            }
        } else {
            return Optional.empty();
        }
    }
    
    private Optional<InvocationCommand> chooseOneCommand(
            Initiator initiator, 
            InvocationCommand command, 
            List<InvocationCommand> commands) {
        
    }

    private Optional<InvocationCommand> chooseOneCommand(
            Initiator initiator, String original, List<InvocationCommand> commands) {
        
    }
    
    @Override
    public Optional<InvocationCommand> findStoredCommandByPatternOfAnyType(
            Initiator initiator, String pattern) {
        
    }
}
