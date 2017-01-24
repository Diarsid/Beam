/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.scriptor;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Diarsid
 */
class ScriptsCatalogReal implements ScriptsCatalog {
    
    ScriptsCatalogReal(String path) {
    }
    
    @Override
    public String getCatalogPath() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<Script> getScripts() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Optional<Script> getScriptByName() {
        throw new UnsupportedOperationException();
    }
}
