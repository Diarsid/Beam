/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.interpreter;

import diarsid.beam.core.base.control.io.commands.Command;

/**
 *
 * @author Diarsid
 */
@FunctionalInterface
public interface Recognizer {
    
    Command assess(Input input);
}
