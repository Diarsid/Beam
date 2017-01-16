/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.CommandType;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.EditableTarget;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.SingleStringCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;
import diarsid.beam.core.domain.entities.validation.ValidationResult;
import diarsid.beam.core.domain.entities.validation.ValidationRule;

import static diarsid.beam.core.control.io.commands.CommandOperationType.EDIT_ENTITY;
import static diarsid.beam.core.control.io.commands.CommandOperationType.FIND_ENTITY;
import static diarsid.beam.core.control.io.commands.CommandOperationType.REMOVE_ENTITY;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_UNDEFINED;
import static diarsid.beam.core.control.io.commands.EditableTarget.argToTarget;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.ENTITY_NAME;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.LOCAL_DIRECTORY_PATH;
import static diarsid.beam.core.domain.entities.validation.ValidationRule.applyValidatationRule;

/**
 *
 * @author Diarsid
 */
public class KeeperDialogHelper {
    
    private final InnerIoEngine ioEngine;
    
    public KeeperDialogHelper(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
    }
    
    public String validateInteractively(
            Initiator initiator, String argument, String ioRequest, ValidationRule rule) {
        ValidationResult result = applyValidatationRule(argument, rule);
        if ( result.isOk() ) {
            return argument;
        } else {
            String anotherValue = "";
            while ( result.isFail() ) {
                this.ioEngine.report(initiator, result.getFailureMessage());
                anotherValue = this.ioEngine.askInput(initiator, ioRequest);            
                if ( anotherValue.isEmpty() ) {
                    return "";
                } 
                result = applyValidatationRule(argument, rule);
            }
            return anotherValue;
        }
    } 

    public String validateEntityNameInteractively(Initiator initiator, String argument) {
        ValidationResult result = applyValidatationRule(argument, ENTITY_NAME);
        if ( result.isOk() ) {
            return argument;
        } else {
            String anotherValue = "";
            while ( result.isFail() ) {
                this.ioEngine.report(initiator, result.getFailureMessage());
                anotherValue = this.ioEngine.askInput(initiator, "name");            
                if ( anotherValue.isEmpty() ) {
                    return "";
                } 
                result = applyValidatationRule(argument, ENTITY_NAME);
            }
            return anotherValue;
        }
    } 
    
    private boolean processCommandWithEntityName(SingleStringCommand command, Initiator initiator) {
        String name;
        if ( command.hasNoArg() ) {
            name = this.ioEngine.askInput(initiator, "name");
            if ( name.isEmpty() ) {
                return false;
            }
        } else {
            name = command.getArg();
        } 
        
        name = this.validateInteractively(initiator, name, "name", ENTITY_NAME);
        if ( name.isEmpty() ) {
            return false;
        } else {
            command.resetArg(name);
            return true;
        }        
    }
    
    public boolean checkFinding(
            Initiator initiator, FindEntityCommand command, CommandType exactType) {
        if ( command.type().isNot(exactType) || exactType.operationType().isNot(FIND_ENTITY) ) {
            return false;
        }
        
        return this.processCommandWithEntityName(command, initiator);
    }
    
    public boolean check(Initiator initiator, CreateLocationCommand command) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return false;
        }
        
        String name;
        if ( command.hasName() ) {
            name = command.getName();
        } else {
            name = this.ioEngine.askInput(initiator, "name");
            if ( name.isEmpty() ) {
                return false;
            }
        }
        name = this.validateEntityNameInteractively(initiator, name);
        if ( name.isEmpty() ) {
            return false;
        } else {
            command.resetName(name);
        }
        
        String path;
        if ( command.hasPath() ) {
            path = command.getPath();
        } else {
            path = this.ioEngine.askInput(initiator, "path");
            if ( path.isEmpty() ) {
                return false;
            }
        }
        path = this.validateInteractively(initiator, path, "path", LOCAL_DIRECTORY_PATH);
        if ( path.isEmpty() ) {
            return false;
        } else {
            command.resetPath(path);
        }
        
        return true;
    }
    
    public boolean checkEdition(
            Initiator initiator, 
            EditEntityCommand command, 
            CommandType exactType, 
            EditableTarget... possibleTargets) {
        if ( 
                command.type().isNot(exactType) || 
                exactType.operationType().isNot(EDIT_ENTITY) ||
                possibleTargets.length == 0 ) {
            return false;
        }
        
        String name;        
        if ( command.hasName() ) { 
            name = this.ioEngine.askInput(initiator, "name");
            if ( name.isEmpty() ) {
                return false;
            }
        } else {
            name = command.getName();
        } 
        
        name = this.validateInteractively(initiator, name, "name", ENTITY_NAME);
        if ( name.isEmpty() ) {
            return false;
        } else {
            command.resetName(name);
        }
        
        if ( ! command.isTargetDefined() ) {
            EditableTarget target = TARGET_UNDEFINED;
            String targetName;
            while ( target.isNotDefined() ) {
                targetName = this.ioEngine.askInput(initiator, "target");
                if ( targetName.isEmpty() ) {
                    return false;
                }
                target = argToTarget(targetName);
                if ( target.isNotDefined() ) {
                    this.ioEngine.report(initiator, "cannot recognize...");
                } else if ( target.isNotOneOf(possibleTargets) ) {
                    this.ioEngine.report(initiator, "not editable in this context.");
                    target = TARGET_UNDEFINED;
                }
            }
            command.resetTarget(target);
        }
        
        return true;
    }
    
    public boolean checkDeletion(
            Initiator initiator, RemoveEntityCommand command, CommandType exactType) {
        if ( command.type().isNot(exactType) || exactType.operationType().isNot(REMOVE_ENTITY) ) {
            return false;
        }
        
        return this.processCommandWithEntityName(command, initiator);
    }
}
