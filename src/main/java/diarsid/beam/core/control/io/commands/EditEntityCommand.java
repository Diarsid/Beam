/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;

import static diarsid.beam.core.control.io.commands.EditableTarget.TARGET_UNDEFINED;
import static diarsid.beam.core.util.StringUtils.nonNullNonEmpty;


public class EditEntityCommand extends DoubleStringCommand {
    
    private final CommandType type;

    public EditEntityCommand(String name, EditableTarget target, CommandType type) {
        super(name, target.name());
        this.type = type;
    }
    
    public boolean isTargetDefined() {
        return 
                super.hasSecond() && 
                ! super.getSecond().equals(TARGET_UNDEFINED.name());
    }
    
    public EditableTarget getTarget() {
        return EditableTarget.valueOf(super.getSecond());
    }
    
    public boolean hasName() {
        return super.hasFirst();
    }
    
    public String getName() {
        return super.getFirst();
    }
    
    public void resetName(String name) {
        if ( nonNullNonEmpty(name) ) {
            super.resetFirst(name);
        }
    }
    
    public void resetTarget(EditableTarget target) {
        if ( target.isDefined() ) {
            super.resetSecond(target.name());
        }        
    }

    @Override
    public CommandType type() {
        return this.type;
    }
}
