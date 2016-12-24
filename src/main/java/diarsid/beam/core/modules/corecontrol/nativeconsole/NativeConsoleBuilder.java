/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol.nativeconsole;

import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.interpreter.CommandLineProcessor;

/**
 *
 * @author Diarsid
 */
public class NativeConsoleBuilder {
    
    public NativeConsoleBuilder() {
    }
    
    public OuterIoEngine build(CommandLineProcessor commandLineProcessor) {
        InputBlockingBuffer buffer = new InputBlockingBuffer();
        NativeConsole nativeConsole = new NativeConsole(commandLineProcessor, buffer);
        return nativeConsole;
    }
}
