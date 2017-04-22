/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntity;

import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_NOT_FOUND;
import static diarsid.beam.core.domain.entities.NamedEntityType.UNDEFINED_ENTITY;


public class ExecutorDefaultCommand implements Command {
    
    private final String argument;

    public ExecutorDefaultCommand(String argument) {
        this.argument = argument;
    }
    
    public String argument() {
        return this.argument;
    }

    @Override
    public CommandType type() {
        return EXECUTOR_DEFAULT;
    }
    
    public Optional<InvocationCommand> mergeWithEntity(
            Optional<? extends NamedEntity> entity) {
        if ( ! entity.isPresent() ) {
            return Optional.empty();
        }
        String entityName = entity.get().name();
        switch ( entity.get().type() ) {
            case LOCATION : {
                return Optional.of(new OpenLocationCommand(
                        this.argument, entityName, NEW, TARGET_FOUND));
            }    
            case WEBPAGE : {
                return Optional.of(new SeePageCommand(
                        this.argument, entityName, NEW, TARGET_FOUND));
            }    
            case PROGRAM : {
                return Optional.of(new RunProgramCommand(
                        this.argument, entityName, NEW, TARGET_FOUND));
            }    
            case BATCH : {
                return Optional.of(new CallBatchCommand(
                        this.argument, entityName, NEW, TARGET_FOUND));
            }    
            case UNDEFINED_ENTITY :
            default : {
                return Optional.empty();
            }
        }
    }
        
    public Optional<InvocationCommand> mergeWithCommand(Optional<InvocationCommand> anotherCommand) {
        if ( ! anotherCommand.isPresent() ) {
            return Optional.empty();
        }
        String extendedArgument = anotherCommand.get().extendedArgument();
        commandMerging: switch ( anotherCommand.get().type() ) {
            case OPEN_LOCATION : {
                return Optional.of(new OpenLocationCommand(
                        this.argument, extendedArgument, NEW, TARGET_NOT_FOUND));
            }
            case OPEN_LOCATION_TARGET : {
                return Optional.of(new OpenLocationTargetCommand(
                        this.argument, extendedArgument, NEW, TARGET_NOT_FOUND));
            }
            case RUN_PROGRAM : {
                return Optional.of(new RunProgramCommand(
                        this.argument, extendedArgument, NEW, TARGET_NOT_FOUND));
            }
            case SEE_WEBPAGE : {
                return Optional.of(new SeePageCommand(
                        this.argument, extendedArgument, NEW, TARGET_NOT_FOUND));
            }
            case CALL_BATCH : {
                return Optional.of(new CallBatchCommand(
                        this.argument, extendedArgument, NEW, TARGET_NOT_FOUND));
            }
            default : {
                return Optional.empty();
            }
        }  
    }
}
