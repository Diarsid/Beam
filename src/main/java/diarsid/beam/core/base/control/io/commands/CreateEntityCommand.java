/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import static diarsid.beam.core.base.control.io.commands.CommandOperationType.CREATE_ENTITY;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.onlyIfCommandHasAppropriateOperationType;

/**
 *
 * @author Diarsid
 */
public class CreateEntityCommand extends EmptyCommand {
    
    public CreateEntityCommand(CommandType type) {
        super(type);
        onlyIfCommandHasAppropriateOperationType(type, CREATE_ENTITY);
    }
}
