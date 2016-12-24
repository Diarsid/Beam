/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.commands;


public abstract class SingleArgumentCommand implements ArgumentedCommand {
    
    private final Argument argument;
    
    public SingleArgumentCommand(String argument) {
        this.argument = new Argument(argument);
    }
    
    public SingleArgumentCommand(String argument, String extendedArgument) {
        this.argument = new Argument(argument, extendedArgument);
    }

    @Override
    public String stringifyOriginal() {
        return this.argument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.argument.getExtended();
    }
    
    public final Argument argument() {
        return this.argument;
    }
}
