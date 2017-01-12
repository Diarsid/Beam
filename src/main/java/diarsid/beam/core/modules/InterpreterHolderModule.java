/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.BeamModule;
import diarsid.beam.core.control.io.interpreter.Interpreter;

/**
 *
 * @author Diarsid
 */
public interface InterpreterHolderModule extends BeamModule {

    Interpreter getInterpreter();
}
