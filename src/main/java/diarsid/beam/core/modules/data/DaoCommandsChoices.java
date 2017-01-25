/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.control.io.commands.CommandType;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsChoices {
    
    Optional<CommandType> getChoiceFor(String pattern);
    
    boolean saveChoice(ArgumentedCommand command);
    
    boolean saveChoice(CommandType type, String pattern);
    
    boolean deleteChoice(ArgumentedCommand command);
    
    boolean deleteChoice(CommandType type, String pattern);
}
