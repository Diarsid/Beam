/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.catalogs;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.os.search.result.FileSearchResult;
import diarsid.beam.core.application.starter.scripts.Script;

/**
 *
 * @author Diarsid
 */
public class ScriptsCatalogReal 
        extends SearcheableCatalog 
        implements ScriptsCatalog {
    
    private final String catalogPath;
    
    ScriptsCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
        this.catalogPath = catalogPath;
    }
    
    @Override
    public String getCatalogPath() {
        return this.catalogPath;
    }
    
    @Override
    public List<Script> getScripts() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Optional<Script> getScriptByName(String name) {
        FileSearchResult result = super.findFileInCatalogByStrictName(name);
        if ( result.isOk() && result.success().hasSingleFoundFile() ) {
            throw new UnsupportedOperationException();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void rewriteScripts() {
        throw new UnsupportedOperationException();
    }
}
