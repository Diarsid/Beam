/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.applicationhome;

/**
 *
 * @author Diarsid
 */
class ApplicationHomeReal implements ApplicationHome {
    
    private final ScriptsCatalog scriptsCatalog;
    private final LibrariesCatalog librariesCatalog;
    private final ProgramsCatalog programsCatalog;
    private final NotesCatalog notesCatalog;

    ApplicationHomeReal(
            ScriptsCatalog scriptsCatalog, 
            LibrariesCatalog librariesCatalog, 
            ProgramsCatalog programsCatalog, 
            NotesCatalog notesCatalog) {
        this.scriptsCatalog = scriptsCatalog;
        this.librariesCatalog = librariesCatalog;
        this.programsCatalog = programsCatalog;
        this.notesCatalog = notesCatalog;
    }

    @Override
    public ScriptsCatalog getScriptsCatalog() {
        return this.scriptsCatalog;
    }

    @Override
    public LibrariesCatalog getLibrariesCatalog() {
        return this.librariesCatalog;
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
