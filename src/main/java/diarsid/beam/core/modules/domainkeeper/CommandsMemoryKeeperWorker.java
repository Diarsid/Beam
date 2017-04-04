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
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenPathCommand;
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
    public void tryToExtendCommand(Initiator initiator, InvocationEntityCommand command) {
        Optional<ExtendableCommand> exactMatch = this.extendingByExactMatch(initiator, command);
        if ( exactMatch.isPresent() ) {
            command.setStored();
            command.argument().setExtended(exactMatch.get().extendedArgument());
        } else {
            command.setNew();
            this.extendingByPattern(initiator, command);
        }
    }   

    public Optional<ExtendableCommand> extendingByExactMatch(
            Initiator initiator, InvocationEntityCommand command) {
        Optional<ExtendableCommand> exactMatch = 
                this.daoCommands.getByExactOriginalAndType(
                        initiator, command.argument().original(), command.type());
        return exactMatch;
    }    
    
    private void extendingByPattern(Initiator initiator, InvocationEntityCommand command) {
        List<ExtendableCommand> foundInOriginal = 
                this.daoCommands.searchInOriginalByPatternAndType(
                        initiator, command.argument().original(), command.type());
        if ( nonEmpty(foundInOriginal) ) {
            this.chooseOneCommandAndUseAsExtension(initiator, foundInOriginal, command);
        } else {
            List<ExtendableCommand> foundInExtended = 
                    this.daoCommands.searchInExtendedByPatternAndType(
                            initiator, command.argument().original(), command.type());
            if ( nonEmpty(foundInExtended) ) {
                this.chooseOneCommandAndUseAsExtension(initiator, foundInExtended, command);
            }
        }
    }

    private void chooseOneCommandAndUseAsExtension(
            Initiator initiator, 
            List<ExtendableCommand> foundCommands, 
            InvocationEntityCommand command) {
        if ( hasOne(foundCommands) ) {
            command.argument().setExtended(getOne(foundCommands).extendedArgument());
        } else {
            Optional<ExtendableCommand> chosenCommand = 
                    this.chooseOneCommand(initiator, command, foundCommands);
            if ( chosenCommand.isPresent() ) {
                command.argument().setExtended(chosenCommand.get().extendedArgument());
            }
        }        
    }

    @Override
    public void save(Initiator initiator, ExtendableCommand command) {
        this.daoCommands.save(initiator, command);
    }

    @Override
    public void remove(Initiator initiator, ExtendableCommand command) {
        this.daoCommands.delete(initiator, command);
    }

    @Override
    public void tryToExtendCommand(Initiator initiator, OpenPathCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<ExtendableCommand> findStoredCommandByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        List<ExtendableCommand> foundCommands = 
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
    
    private Optional<ExtendableCommand> chooseOneCommand(
            Initiator initiator, 
            InvocationEntityCommand command, 
            List<ExtendableCommand> commands) {
        
    }

    private Optional<ExtendableCommand> chooseOneCommand(
            Initiator initiator, String original, List<ExtendableCommand> commands) {
        
    }
}
