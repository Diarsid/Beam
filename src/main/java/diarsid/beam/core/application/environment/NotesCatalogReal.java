/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import diarsid.beam.core.base.os.search.FileSearcher;


public class NotesCatalogReal 
        extends SearcheableCatalog 
        implements NotesCatalog {

    NotesCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
    }
}
