/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import static diarsid.beam.core.application.configuration.ApplicationConfiguration.getConfiguration;
import static diarsid.beam.core.application.environment.ScriptSyntax.getScriptSyntax;
import static diarsid.beam.core.base.os.search.FileSearcher.getSearcherWithDepthsOf;

/**
 *
 * @author Diarsid
 */
public class ApplicationCatalogs {

    private ApplicationCatalogs() {
    }

    public static ScriptsCatalog getScriptsCatalog() {
        return new ScriptsCatalogReal(
                ".", getLibrariesCatalog(), getConfiguration(), getScriptSyntax())
                .refreshScripts();
    }

    public static LibrariesCatalog getLibrariesCatalog() {
        return new LibrariesCatalogReal(".", "../lib");
    }

    public static ProgramsCatalog getProgramsCatalog() {
        return new ProgramsCatalogReal(
                getConfiguration().getAsString("catalogs.programs"), 
                getSearcherWithDepthsOf(3, 3));
    }

    public static NotesCatalog getNotesCatalog() {
        return new NotesCatalogReal(
                getConfiguration().getAsString("catalogs.notes"), 
                getSearcherWithDepthsOf(3, 3));
    }
    
}
