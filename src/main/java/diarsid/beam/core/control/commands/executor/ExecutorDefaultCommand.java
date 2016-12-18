/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.SingleArgumentCommand;

import static diarsid.beam.core.control.commands.CommandType.EXECUTOR_DEFAULT;


public class ExecutorDefaultCommand extends SingleArgumentCommand {

    public ExecutorDefaultCommand(String argument) {
        super(argument);
    }

    @Override
    public CommandType getType() {
        return EXECUTOR_DEFAULT;
    }
}
