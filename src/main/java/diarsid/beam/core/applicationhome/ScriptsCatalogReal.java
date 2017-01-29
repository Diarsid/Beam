/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.applicationhome;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.os.search.FileSearcher;
import diarsid.beam.core.os.search.result.FileSearchResult;
import diarsid.beam.core.scriptor.Script;

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
        
    }
    
    @Override
    public Optional<Script> getScriptByName(String name) {
        FileSearchResult result = super.findFileInCatalogByStrictName(name);
        if ( result.isOk() && result.success().hasSingleFoundFile() ) {
            
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void rewriteScripts() {
        
    }
}