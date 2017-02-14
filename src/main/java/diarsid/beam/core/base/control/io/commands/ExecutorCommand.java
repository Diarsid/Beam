/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import static diarsid.beam.core.base.control.io.commands.CommandOperationType.EXECUTOR_OPERATION;
import static diarsid.beam.core.base.control.io.commands.CommandOperationType.onlyIfCommandHasAppropriateOperationType;


public class ExecutorCommand extends SingleStringCommand {

    public ExecutorCommand(String arg, CommandType type) {
        super(arg, type);
        onlyIfCommandHasAppropriateOperationType(type, EXECUTOR_OPERATION);
    }
}
