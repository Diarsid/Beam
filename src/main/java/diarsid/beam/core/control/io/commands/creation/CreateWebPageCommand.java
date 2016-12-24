/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands.creation;

import diarsid.beam.core.control.io.commands.CommandType;
import diarsid.beam.core.control.io.commands.TripleStringCommand;

import static diarsid.beam.core.control.io.commands.CommandType.CREATE_PAGE;


public class CreateWebPageCommand extends TripleStringCommand {
        
    public CreateWebPageCommand(String name, String url, String place) {
        super(name, url, place);
    }

    public String getName() {
        return super.getFirst();
    }

    public String getUrl() {
        return super.getSecond();
    }
    
    public String getPlace() {
        return super.getThird();
    }
    
    public boolean hasName() {
        return super.hasFirst();
    }
    
    public boolean hasUrl() {
        return super.hasSecond();
    }
    
    public boolean hasPlace() {
        return super.hasThird();
    }

    @Override
    public CommandType getType() {
        return CREATE_PAGE;
    }
}
