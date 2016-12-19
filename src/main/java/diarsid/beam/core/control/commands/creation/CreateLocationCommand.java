/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.creation;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.DoubleStringCommand;

import static diarsid.beam.core.control.commands.CommandType.CREATE_LOCATION;


public class CreateLocationCommand extends DoubleStringCommand {
        
    public CreateLocationCommand(String name, String path) {        
        super(name, path);
    }
    
    public String getPath() {
        return super.getSecond();
    }
    
    public String getName() {
        return super.getFirst();
    }
    
    public boolean hasName() {
        return super.hasFirst();
    }
    
    public boolean hasPath() {
        return super.hasSecond();
    }

    @Override
    public CommandType getType() {
        return CREATE_LOCATION;
    }
}
