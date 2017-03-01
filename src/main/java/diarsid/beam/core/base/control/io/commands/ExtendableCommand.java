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
    
    String stringifyOriginalArgs();
    
    String stringifyExtendedArgs();
    
    String stringify();
}
