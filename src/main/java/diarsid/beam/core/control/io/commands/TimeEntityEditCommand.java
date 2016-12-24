/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;


public class TimeEntityEditCommand extends DoubleStringCommand {

    private final CommandType type;
    
    public TimeEntityEditCommand(String time, String text, CommandType type) {
        super(time, text);
        this.type = type;
    }
    
    public String getTime() {
        return super.getFirst();
    }
    
    public String getText() {
        return super.getSecond();
    }
    
    public boolean hasTime() {
        return super.hasFirst();
    }
    
    public boolean hasText() {
        return super.hasSecond();
    }

    @Override
    public CommandType getType() {
        return this.type;
    }
}
