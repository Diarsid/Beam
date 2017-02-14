/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import static java.util.Objects.isNull;


public abstract class StringCommand implements Command {
    
    protected StringCommand() {
    }

    protected void onlyNonNullArgument(String arg) {
        if ( isNull(arg) ) {
            throw new NullPointerException("Command argument cannot be null.");
        }
    }
}
