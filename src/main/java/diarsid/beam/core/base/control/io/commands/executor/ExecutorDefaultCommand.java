/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import java.util.Optional;

import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.EntityInvocationCommand;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.domain.entities.NamedEntityType.UNDEFINED_ENTITY;


public class ExecutorDefaultCommand extends EntityInvocationCommand {

    public ExecutorDefaultCommand(String argument) {
        super(argument);
    }
    
    public ExecutorDefaultCommand(String argument, String extended) {
        super(argument, extended);
    }

    @Override
    public CommandType type() {
        return EXECUTOR_DEFAULT;
    }
    
    @Override
    public Variant toVariant(int variantIndex) {
        return new Variant(this.stringify(), variantIndex);
    }

    @Override
    public String stringify() {
        return super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return UNDEFINED_ENTITY;
    }
    
    public Optional<EntityInvocationCommand> mergeWithEntity(
            Optional<? extends NamedEntity> entity) {
        if ( ! entity.isPresent() ) {
            return Optional.empty();
        }
        switch ( entity.get().type() ) {
            case LOCATION : {
                return Optional.of(new OpenLocationCommand(
                        this.originalArgument(), entity.get().name()));
            }    
            case WEBPAGE : {
                return Optional.of(new SeePageCommand(
                        this.originalArgument(), entity.get().name()));
            }    
            case PROGRAM : {
                return Optional.of(new RunProgramCommand(
                        this.originalArgument(), entity.get().name()));
            }    
            case BATCH : {
                return Optional.of(new CallBatchCommand(
                        this.originalArgument(), entity.get().name()));
            }    
            case UNDEFINED_ENTITY:
            default : {
                return Optional.empty();
            }
        }
    }
        
    public Optional<ExtendableCommand> mergeWithCommand(Optional<ExtendableCommand> anotherCommand) {
        if ( anotherCommand.isPresent() ) {
            ExtendableCommand mergedCommand;
            commandMerging: switch ( anotherCommand.get().type() ) {
                case OPEN_LOCATION : {
                    mergedCommand = new OpenLocationCommand(
                            super.originalArgument(), 
                            anotherCommand.get().extendedArgument());
                    mergedCommand.setNew();
                    break commandMerging;
                }
                case OPEN_LOCATION_TARGET : {
                    mergedCommand = new OpenLocationTargetCommand(
                            super.originalArgument(), 
                            anotherCommand.get().extendedArgument());
                    mergedCommand.setNew();
                    break commandMerging;
                }
                case RUN_PROGRAM : {
                    mergedCommand = new RunProgramCommand(
                            super.originalArgument(), 
                            anotherCommand.get().extendedArgument());
                    mergedCommand.setNew();
                    break commandMerging;
                }
                case SEE_WEBPAGE : {
                    mergedCommand = new SeePageCommand(
                            super.originalArgument(), 
                            anotherCommand.get().extendedArgument());
                    mergedCommand.setNew();
                    break commandMerging;
                }
                case CALL_BATCH : {
                    mergedCommand = new CallBatchCommand(
                            super.originalArgument(), 
                            anotherCommand.get().extendedArgument());
                    mergedCommand.setNew();
                    break commandMerging;
                }
                default : {
                    mergedCommand = null;
                }
            }  
            return Optional.ofNullable(mergedCommand);
        } else {
            return Optional.empty();
        }
    }
}
