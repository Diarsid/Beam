/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.ValueOperation;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.modules.DataModule;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Operations.valueCompletedWith;
import static diarsid.beam.core.base.control.flow.Operations.valueOperationFail;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.joinToOptionalMessage;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.linesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.tasksToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_ALL;
import static diarsid.beam.core.base.util.StringUtils.lower;


class AllKeeperWorker implements AllKeeper {
    
    private final DataModule data;
    private final ProgramsKeeper programs;

    AllKeeperWorker(DataModule dataModule, ProgramsKeeper programsKeeper) {
        this.data = dataModule;
        this.programs = programsKeeper;
    }

    @Override
    public ValueOperation<Message> findAll(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_ALL) ) {
            return valueOperationFail("wrong command type!");
        }
        
        return valueCompletedWith(this.collectAll(initiator, command.joinedArguments()));
    }
    
    private Optional<Message> collectAll(Initiator initiator, String argument) {
        List<Optional<Message>> messages = new ArrayList<>();
        
        messages.add(this.collectTasks(initiator, argument));
        messages.add(this.collectCommands(initiator, argument));
        messages.add(this.collectLocations(initiator, argument));
        messages.add(this.collectBatches(initiator, argument));
        messages.add(this.collectWebPages(initiator, argument));
        messages.add(this.collectPrograms(initiator, argument));
        messages.add(this.collectWebDirectories(initiator, argument));
        
        return joinToOptionalMessage(messages
                .stream()
                .filter(message -> message.isPresent())
                .map(message -> message.get())
                .collect(toList()));
    }
    
    private Optional<Message> collectTasks(Initiator initiator, String argument) {
        return tasksToOptionalMessageWithHeader("Tasks", this.data
                .tasks()
                .findTasksByTextPattern(initiator, argument));
    }
    
    private Optional<Message> collectCommands(Initiator initiator, String argument) {
        List<InvocationCommand> commands = new ArrayList<>();        
        commands.addAll(this.data.commands().searchInOriginalByPattern(initiator, argument));
        if ( commands.isEmpty() ) {
            commands.addAll(this.data.commands().searchInExtendedByPattern(initiator, argument));
        }        
        return linesToOptionalMessageWithHeader("Commands:", commands
                .stream()
                .map(command -> command.toMessageString())
                .collect(toList()));
    }
    
    private Optional<Message> collectLocations(Initiator initiator, String argument) {
        return linesToOptionalMessageWithHeader("Locations:", this.data
                .locations()
                .getLocationsByNamePattern(initiator, argument)
                .stream()
                .map(location -> location.name())
                .collect(toList()));
    }
    
    private Optional<Message> collectBatches(Initiator initiator, String argument) {
        return linesToOptionalMessageWithHeader("Batches:", this.data
                .batches()
                .getBatchNamesByNamePattern(initiator, argument));
    }
    
    private Optional<Message> collectWebPages(Initiator initiator, String argument) {
        return linesToOptionalMessageWithHeader("WebPages:", this.data
                .webPages()
                .findByPattern(initiator, argument)
                .stream()
                .map(page -> page.name())
                .collect(toList()));
    }
    
    private Optional<Message> collectWebDirectories(Initiator initiator, String argument) {
        return linesToOptionalMessageWithHeader("WebDirectories:", this.data
                .webDirectories()
                .findDirectoriesByPatternInAnyPlace(initiator, argument)
                .stream()
                .map(webDir -> format("%s (%s)", webDir.name(), lower(webDir.place().name())))
                .collect(toList()));
    }
    
    private Optional<Message> collectPrograms(Initiator initiator, String argument) {
        return linesToOptionalMessageWithHeader("Programs:", this.programs
                .getProgramsByPattern(initiator, argument)
                .stream()
                .map(program -> program.name())
                .collect(toList()));
    }
}
