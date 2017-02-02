/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import diarsid.beam.core.application.NotesCatalog;
import diarsid.beam.core.application.ProgramsCatalog;
import diarsid.beam.core.config.Configuration;
import diarsid.beam.core.control.io.interpreter.Interpreter;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ApplicationComponentsHolderModule extends GemModule {
    
    Configuration getConfiguration();
    
    Interpreter getInterpreter();
    
    ProgramsCatalog getProgramsCatalog();
    
    NotesCatalog getNotesCatalog();
}
