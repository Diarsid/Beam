/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands;

/**
 *
 * @author Diarsid
 */
public interface ArgumentedCommand extends Command {
    
    String stringifyOriginal();
    
    String stringifyExtended();
}
