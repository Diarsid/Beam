/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import static diarsid.beam.core.control.io.commands.CommandOperationType.REMOVE_ENTITY;
import static diarsid.beam.core.control.io.commands.CommandOperationType.onlyIfCommandHasAppropriateOperationType;


public class RemoveEntityCommand extends SingleStringCommand {

    public RemoveEntityCommand(String arg, CommandType type) {
        super(arg, type);
        onlyIfCommandHasAppropriateOperationType(type, REMOVE_ENTITY);
    }
}
