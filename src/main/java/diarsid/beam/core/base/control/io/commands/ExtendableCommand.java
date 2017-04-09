/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToVariant;

/**
 *
 * @author Diarsid
 */
public interface ExtendableCommand extends Command, ConvertableToVariant {
    
    String originalArgument();
    
    String extendedArgument();
    
    String stringify();
    
    ExtendableCommand setNew();
    
    ExtendableCommand setStored();
    
    boolean wasNotUsedBefore();
    
    boolean wasUsedBeforeAndStored();
    
    ExtendableCommand setTargetFound();
    
    ExtendableCommand setTargetNotFound();
    
    boolean isTargetFound();
    
    boolean isTargetNotFound();
}
