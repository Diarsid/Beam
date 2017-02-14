/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.creation;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.DoubleStringCommand;
import diarsid.beam.core.domain.entities.WebPlace;

import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE_DIR;
import static diarsid.beam.core.domain.entities.WebPlace.valueOf;


public class CreateWebDirectoryCommand extends DoubleStringCommand {
    
    public CreateWebDirectoryCommand(String dirName) {
        super(dirName, "");
    }
    
    public CreateWebDirectoryCommand(String dirName, String place) {
        super(dirName, place);
    }
    
    public String getName() {
        return super.getFirst();
    }

    public WebPlace getPlacement() {
        return valueOf(super.getSecond());
    }
    
    public boolean hasName() {
        return super.hasFirst();
    }
    
    public boolean hasPlace() {
        return super.hasSecond();
    }
    
    @Override
    public CommandType type() {
        return CREATE_PAGE_DIR;
    }
}
