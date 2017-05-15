/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import static diarsid.beam.core.application.environment.ConfigurationReading.CONFIGURATION;
import static diarsid.beam.core.base.os.search.FileSearcher.searcherWithDepthsOf;
import static diarsid.beam.core.application.environment.ScriptSyntax.scriptSyntax;

/**
 *
 * @author Diarsid
 */
public class BeamEnvironment {

    private BeamEnvironment() {
    }

    public static ScriptsCatalog scriptsCatalog() {
        return new ScriptsCatalogReal(
                ".", librariesCatalog(), configuration(), scriptSyntax())
                .refreshScripts();
    }

    public static LibrariesCatalog librariesCatalog() {
        return new LibrariesCatalogReal(".", "../lib");
    }

    public static ProgramsCatalog programsCatalog() {
        return new ProgramsCatalogReal(
                configuration().asString("catalogs.programs"), 
                searcherWithDepthsOf(3, 3));
    }

    public static NotesCatalog notesCatalog() {
        return new NotesCatalogReal(
                configuration().asString("catalogs.notes"), 
                searcherWithDepthsOf(3, 3));
    }
    
    public static Configuration configuration() {
        return CONFIGURATION;
    }
    
}