/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.creation;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.DoubleStringCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;


public class CreateTaskCommand extends DoubleStringCommand {    
    
    public CreateTaskCommand(String timeString, String taskString) {
        super(timeString, taskString);
    }
    
    public boolean hasTime() {
        return super.hasFirst();
    }
    
    public boolean hasTask() {
        return super.hasSecond();
    }

    public String getTimeString() {
        return this.getFirst();
    }

    public String getTaskString() {
        return this.getSecond();
    }

    @Override
    public CommandType type() {
        return CREATE_TASK;
    }
}
