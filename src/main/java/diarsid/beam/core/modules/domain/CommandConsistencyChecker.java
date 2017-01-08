/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domain;

import java.nio.file.Paths;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.EditEntityCommand;
import diarsid.beam.core.control.io.commands.FindEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateLocationCommand;

import static diarsid.beam.core.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.util.PathUtils.pathIsDirectory;

/**
 *
 * @author Diarsid
 */
public class CommandConsistencyChecker {
    
    private final InnerIoEngine ioEngine;
    
    public CommandConsistencyChecker(InnerIoEngine ioEngine) {
        this.ioEngine = ioEngine;
    }
    
    public boolean check(FindEntityCommand command, Initiator initiator) {
        if ( command.type().isNot(FIND_LOCATION) ) {
            return false;
        }
        if ( command.hasNoArg() ) {
            String input = this.ioEngine.askForInput(initiator, "name");
            if ( input.isEmpty() ) {
                return false;
            } else {
                command.resetArg(input);                
            }
        } else {
            return false;
        }   
        return true;
    }
    
    public boolean check(CreateLocationCommand command, Initiator initiator) {
        if ( command.type().isNot(CREATE_LOCATION) ) {
            return false;
        }
        
        if ( command.hasName() ) {
            
        } else {
            String input = this.ioEngine.askForInput(initiator, "name");
            if ( input.isEmpty() ) {
                return false;
            } else {
                command.resetName(input);
            }
        }
        
        if ( command.hasPath() ) {
            
        } else {
            String input = this.ioEngine.askForInput(initiator, "path");
            if ( pathIsDirectory(Paths.get(input)) ) {
                command.resetPath(input);
            } else {
                return false;
            }
        }
        return true;
    }
    
    public boolean check(Initiator initiator, EditEntityCommand command) {
        
    }
}
