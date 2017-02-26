/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.catalogs;

import static diarsid.beam.core.application.configuration.ApplicationConfiguration.getConfiguration;
import static diarsid.beam.core.base.os.search.FileSearcher.getSearcherWithDepthsOf;

/**
 *
 * @author Diarsid
 */
public class ApplicationCatalogs {

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
                getConfiguration().getAsString("catalogs.programs"), 
                getSearcherWithDepthsOf(3, 3));
    }

    public static NotesCatalog getNotesCatalog() {
        throw new UnsupportedOperationException();
    }
    
}
