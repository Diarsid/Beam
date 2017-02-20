/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.ExtendableCommonCommand;

import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;


public class OpenLocationCommand extends ExtendableCommonCommand {
        
    public OpenLocationCommand(String originalLocation) {
        super(originalLocation);
    }
    
    public OpenLocationCommand(String originalLocation, String extendedLocation) {
        super(originalLocation, extendedLocation);
    }
    
    @Override
    public CommandType type() {
        return OPEN_LOCATION;
    }
}
