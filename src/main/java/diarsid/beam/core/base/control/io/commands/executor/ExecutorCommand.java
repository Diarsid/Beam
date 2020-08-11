/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToMessage;
import diarsid.beam.core.base.analyze.variantsweight.ConvertableToVariant;
import diarsid.beam.core.base.control.io.commands.Command;

/**
 *
 * @author Diarsid
 */
public interface ExecutorCommand 
        extends 
                Command, 
                ConvertableToVariant, 
                ConvertableToMessage {
    
    boolean isInvocation();
    
    public String stringifyOriginal(); 
    
    public String stringify();
    
    String originalArgument();    
}
