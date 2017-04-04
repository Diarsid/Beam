/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.InvocationEntityCommand;
import diarsid.beam.core.base.os.search.result.FileSearchResult;
import diarsid.beam.core.domain.entities.Program;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;


class ProgramsKeeperWorker 
        implements 
                ProgramsKeeper, 
                NamedEntitiesKeeper {
    
    private final InnerIoEngine ioEngine;
    private final ProgramsCatalog programsCatalog;
    private final KeeperDialogHelper helper;
    private final Set<CommandType> operatingCommandTypes;
    
    ProgramsKeeperWorker(
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog, 
            KeeperDialogHelper keeperDialogHelper) {
        this.ioEngine = ioEngine;
        this.programsCatalog = programsCatalog;
        this.helper = keeperDialogHelper;
        this.operatingCommandTypes = toSet(RUN_PROGRAM);
    }

    @Override
    public boolean isSubjectedTo(InvocationEntityCommand command) {
        return this.operatingCommandTypes.contains(command.type());
    }

    @Override
    public Optional<Program> findByExactName(Initiator initiator, String strictName) {
        FileSearchResult result = this.programsCatalog.findProgramByStrictName(strictName);
        if ( result.isOk() && result.success().hasSingleFoundFile() ) {
            return this.optionalProgram(result.success().getFoundFile());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Program> findProgram(Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_PROGRAM) ) {
            return Optional.empty();
        }
        
        String name;
        if ( command.hasArguments()) {
            name = command.getFirstArg();
        } else {
            name = "";
        }
        
        name = this.helper.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return Optional.empty();
        }
        
        return this.findByNamePattern(initiator, name);        
    }

    @Override
    public Optional<Program> findByNamePattern(Initiator initiator, String pattern) {
        FileSearchResult result = this.programsCatalog.findProgramByPattern(pattern);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                return this.optionalProgram(result.success().getFoundFile());
            } else {
                Question question = question("choose program")
                        .withAnswerStrings(result.success().getMultipleFoundFiles());
                Answer answer = this.ioEngine.ask(initiator, question);
                if ( answer.isGiven() ) {
                    return this.optionalProgram(answer.text());
                } else {
                    return Optional.empty();
                }
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<Program> optionalProgram(String programName) {
        return Optional.of(new Program(this.programsCatalog, programName));
    }

    @Override
    public List<Program> getProgramsByPattern(Initiator initiator, String pattern) {
        FileSearchResult result = this.programsCatalog.findProgramByPattern(pattern);
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                return asList(
                        new Program(
                                this.programsCatalog, 
                                result.success().getFoundFile()));
            } else {
                return result
                        .success()
                        .getMultipleFoundFiles()
                        .stream()
                        .map(programFileName -> new Program(this.programsCatalog, programFileName))
                        .collect(toList());
            }
        } else {
            return emptyList();
        }
    }
}
