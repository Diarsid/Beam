/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.domainkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.analyze.variantsweight.Analyze;
import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.modules.ResponsiveDataModule;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.joinToOptionalMessage;
import static diarsid.beam.core.base.control.io.base.interaction.Messages.linesToOptionalMessageWithHeader;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_ALL;
import static diarsid.beam.core.base.util.CollectionsUtils.shrink;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.base.util.TextUtil.shorterStringsFirstNotCountingSpaces;


class AllKeeperWorker implements AllKeeper {
    
    private final ResponsiveDataModule data;
    private final ProgramsKeeper programs;
    private final Analyze analyze;

    AllKeeperWorker(
            ResponsiveDataModule dataModule, ProgramsKeeper programsKeeper, Analyze analyze) {
        this.data = dataModule;
        this.programs = programsKeeper;
        this.analyze = analyze;
    }

    @Override
    public ValueFlow<Message> findAll(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_ALL) ) {
            return valueFlowFail("wrong command type!");
        }
        
        return valueFlowCompletedWith(this.collectAll(initiator, command.joinedArguments()));
    }
    
    private Optional<Message> collectAll(Initiator initiator, String argument) {
        List<Optional<Message>> messages = new ArrayList<>();
        
        messages.add(this.collectLocations(initiator, argument));
        messages.add(this.collectBatches(initiator, argument));
        messages.add(this.collectWebPages(initiator, argument));
        messages.add(this.collectPrograms(initiator, argument));
        messages.add(this.collectWebDirectories(initiator, argument));
        messages.add(this.collectTasks(initiator, argument));
        messages.add(this.collectCommands(initiator, argument));
        
        return joinToOptionalMessage(messages
                .stream()
                .filter(message -> message.isPresent())
                .map(message -> message.get())
                .collect(toList()));
    }
    
    private Optional<Message> collectTasks(Initiator initiator, String argument) {
        List<Message> tasks = this.data
                .tasks()
                .findTasksByTextPattern(initiator, argument)
                .stream()
                .map(task -> task.toMessage())
                .collect(toList());
        
        boolean shrinked = false;
        int shrinkedQty = 0;
        if ( tasks.size() > 4 ) {
            shrinked = true;
            int previousSize = tasks.size();
            shrink(tasks, 4);
            shrinkedQty = previousSize - tasks.size();
        }
        
        List<String> tasksMessages = tasks
                .stream()
                .map(task -> task.allLines())
                .peek(allLines -> allLines.add(allLines.size(), ""))
                .flatMap(allLines -> allLines.stream())
                .collect(toList());
        
        if ( shrinked ) {
            tasksMessages.add(tasksMessages.size(), format("...%s more", shrinkedQty));
        }
        
        return linesToOptionalMessageWithHeader("Tasks:", tasksMessages);
    }
    
    private Optional<Message> stringifiedResultsToMessage(String header, List<String> messages) {
        if ( messages.size() > 7 ) {
            int previousSize = messages.size();
            shrink(messages, 7);
            messages.add(7, format("...%s more", previousSize - messages.size()));
        } 
        
        return linesToOptionalMessageWithHeader(header, messages);
    }
    
    private Optional<Message> collectCommands(Initiator initiator, String argument) {
        List<InvocationCommand> commands = new ArrayList<>();        
        commands.addAll(this.data.commands().searchInOriginalByPattern(initiator, argument));
        if ( commands.isEmpty() ) {
            commands.addAll(this.data.commands().searchInExtendedByPattern(initiator, argument));
        }
        return stringifiedResultsToMessage("Commands:", commands
                .stream()
                .filter(command -> this.analyze.isNameSatisfiable(argument, command.bestArgument()))
                .map(command -> command.toMessageString())
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
    
    private Optional<Message> collectLocations(Initiator initiator, String argument) {
        return stringifiedResultsToMessage("Locations:", this.data
                .locations()
                .getLocationsByNamePattern(initiator, argument)
                .stream()
                .filter(location -> this.analyze.isEntitySatisfiable(argument, location))
                .map(location -> location.name())
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
    
    private Optional<Message> collectBatches(Initiator initiator, String argument) {
        return stringifiedResultsToMessage("Batches:", this.data
                .batches()
                .getBatchNamesByNamePattern(initiator, argument)
                .stream()
                .filter(batchName -> this.analyze.isNameSatisfiable(argument, batchName))
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
    
    private Optional<Message> collectWebPages(Initiator initiator, String argument) {
        return stringifiedResultsToMessage("WebPages:", this.data
                .webPages()
                .findByPattern(initiator, argument)
                .stream()
                .filter(page -> this.analyze.isEntitySatisfiable(argument, page))
                .map(page -> page.name())
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
    
    private Optional<Message> collectWebDirectories(Initiator initiator, String argument) {
        return stringifiedResultsToMessage("WebDirectories:", this.data
                .webDirectories()
                .findDirectoriesByPatternInAnyPlace(initiator, argument)
                .stream()
                .filter(webDir -> this.analyze.isNameSatisfiable(argument, webDir.name()))
                .map(webDir -> format("%s (%s)", webDir.name(), lower(webDir.place().name())))
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
    
    private Optional<Message> collectPrograms(Initiator initiator, String argument) {
        return stringifiedResultsToMessage("Programs:", this.programs
                .getProgramsByPattern(initiator, argument)
                .stream()
                .filter(program -> this.analyze.isEntitySatisfiable(argument, program))
                .map(program -> program.name())
                .sorted(shorterStringsFirstNotCountingSpaces())
                .collect(toList()));
    }
}
