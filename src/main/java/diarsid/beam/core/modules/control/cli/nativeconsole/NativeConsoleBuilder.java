/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.control.cli.nativeconsole;

import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;

/**
 *
 * @author Diarsid
 */
public class NativeConsoleBuilder {
    
    public NativeConsoleBuilder() {
    }
    
    public OuterIoEngine build(CommandLineProcessor commandLineProcessor) {
        InputManager buffer = new InputManager();
        NativeConsole nativeConsole = new NativeConsole(commandLineProcessor, buffer);
        return nativeConsole;
    }
}
