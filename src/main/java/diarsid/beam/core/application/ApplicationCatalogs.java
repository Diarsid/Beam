/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application;

import diarsid.beam.core.config.Configuration;

import static diarsid.beam.core.config.Config.PROGRAMS_LOCATION;
import static diarsid.beam.core.config.Configuration.getConfiguration;
import static diarsid.beam.core.os.search.FileSearcher.getSearcherWithDepthsOf;

/**
 *
 * @author Diarsid
 */
public class ApplicationCatalogs {
    
    private static final Configuration CONFIGURATION;
    static {
        CONFIGURATION = getConfiguration();
    }

    private ApplicationCatalogs() {
    }

    public static ScriptsCatalog getScriptsCatalog() {
        return new ScriptsCatalogReal("./", getSearcherWithDepthsOf(3, 3));
    }

    public static LibrariesCatalog getLibrariesCatalog() {
        throw new UnsupportedOperationException();
    }

    public static ProgramsCatalog getProgramsCatalog() {
        return new ProgramsCatalogReal(
                CONFIGURATION.get(PROGRAMS_LOCATION), 
                getSearcherWithDepthsOf(3, 3));
    }

    public static NotesCatalog getNotesCatalog() {
        throw new UnsupportedOperationException();
    }
    
}
