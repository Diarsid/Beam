/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

import diarsid.beam.core.modules.executor.entities.StoredExecutorCommand;

/**
 *
 * @author Diarsid
 */
public interface ProcessorCommands {

    boolean deleteCommand(String commandName);

    List<StoredExecutorCommand> getAllCommands();

    StoredExecutorCommand getCommand(String name);

    List<StoredExecutorCommand> getCommands(String commandName);

    void newCommand(List<String> commands, String commandName);    
}
