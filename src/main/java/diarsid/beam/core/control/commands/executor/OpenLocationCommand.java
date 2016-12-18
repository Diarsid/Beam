/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.CommandType;
import diarsid.beam.core.control.commands.SingleArgumentCommand;

import static diarsid.beam.core.control.commands.CommandType.OPEN_LOCATION;


public class OpenLocationCommand extends SingleArgumentCommand {
        
    public OpenLocationCommand(String originalLocation) {
        super(originalLocation);
    }
    
    public OpenLocationCommand(String originalLocation, String extendedLocation) {
        super(originalLocation, extendedLocation);
    }
    
    @Override
    public CommandType getType() {
        return OPEN_LOCATION;
    }
}