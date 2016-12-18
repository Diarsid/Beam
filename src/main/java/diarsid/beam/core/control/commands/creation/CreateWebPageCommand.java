/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.creation;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.DoubleStringCommand;

import static diarsid.beam.core.control.commands.CommandType.CREATE_PAGE;


public class CreateWebPageCommand extends DoubleStringCommand {
        
    public CreateWebPageCommand(String name, String url) {
        super(name, url);
    }

    public String getName() {
        return super.getFirst();
    }

    public String getUrl() {
        return super.getSecond();
    }
    
    public boolean hasName() {
        return super.hasFirst();
    }
    
    public boolean hasUrl() {
        return super.hasSecond();
    }

    @Override
    public CommandType getType() {
        return CREATE_PAGE;
    }
}
