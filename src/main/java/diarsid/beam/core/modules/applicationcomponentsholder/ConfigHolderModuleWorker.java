/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.applicationcomponentsholder;

import diarsid.beam.core.application.catalogs.ApplicationCatalogs;
import diarsid.beam.core.application.catalogs.NotesCatalog;
import diarsid.beam.core.application.catalogs.ProgramsCatalog;
import diarsid.beam.core.application.configuration.ApplicationConfiguration;
import diarsid.beam.core.application.configuration.Configuration;
import diarsid.beam.core.base.control.io.interpreter.Interpreter;
import diarsid.beam.core.modules.ApplicationComponentsHolderModule;

/**
 * Module intended to convey configuration parameters 
 * across all program where they are required.
 * 
 * It is devised to avoid static classes and methods usage and 
 * clarify real dependencies of modules.
 * 
 * @author Diarsid
 */
class ConfigHolderModuleWorker implements ApplicationComponentsHolderModule {
    
    private final Configuration configuration;  
    private final Interpreter interpreter;
    private final ProgramsCatalog programsCatalog;
    private final NotesCatalog notesCatalog;
    
    ConfigHolderModuleWorker() {    
        this.configuration = ApplicationConfiguration.getConfiguration();
        this.interpreter = new Interpreter();
        this.programsCatalog = ApplicationCatalogs.getProgramsCatalog();
        this.notesCatalog = ApplicationCatalogs.getNotesCatalog();
    }
    
    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Interpreter getInterpreter() {
        return this.interpreter;
    }

    @Override
    public ProgramsCatalog getProgramsCatalog() {
        return this.programsCatalog;
    }

    @Override
    public NotesCatalog getNotesCatalog() {
        return this.notesCatalog;
    }
}
