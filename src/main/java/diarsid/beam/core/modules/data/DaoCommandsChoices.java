/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Optional;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;

/**
 *
 * @author Diarsid
 */
public interface DaoCommandsChoices {
    
    Optional<CommandType> getChoiceFor(String pattern);
    
    boolean saveChoice(InvocationCommand command);
    
    boolean saveChoice(CommandType type, String pattern);
    
    boolean deleteChoice(InvocationCommand command);
    
    boolean deleteChoice(CommandType type, String pattern);
}
