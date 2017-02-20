/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsChoices {
    
    Optional<CommandType> getChoiceFor(String pattern);
    
    boolean saveChoice(ExtendableCommand command);
    
    boolean saveChoice(CommandType type, String pattern);
    
    boolean deleteChoice(ExtendableCommand command);
    
    boolean deleteChoice(CommandType type, String pattern);
}
