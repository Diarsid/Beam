/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.applicationcomponentsholder;

import diarsid.beam.core.application.environment.BeamEnvironment;
import diarsid.beam.core.application.environment.NotesCatalog;
import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;
import diarsid.support.configuration.Configuration;

/**
 * Module intended to convey configuration parameters 
 * across all program where they are required.
 * 
 * It is devised to avoid static classes and methods usage and 
 * clarify real dependencies of modules.
 * 
 * @author Diarsid
 */
class ApplicationComponentsHolderModuleWorker implements ApplicationComponentsHolderModule {
    
    private final Configuration configuration;  
    private final Interpreter interpreter;
    private final ProgramsCatalog programsCatalog;
    private final NotesCatalog notesCatalog;
    
    ApplicationComponentsHolderModuleWorker() {    
        this.configuration = BeamEnvironment.configuration();
        this.interpreter = new Interpreter();
        this.programsCatalog = BeamEnvironment.programsCatalog();
        this.notesCatalog = BeamEnvironment.notesCatalog();
    }
    
    @Override
    public Configuration configuration() {
        return this.configuration;
    }

    @Override
    public Interpreter interpreter() {
        return this.interpreter;
    }

    @Override
    public ProgramsCatalog programsCatalog() {
        return this.programsCatalog;
    }

    @Override
    public NotesCatalog notesCatalog() {
        return this.notesCatalog;
    }
    
}
