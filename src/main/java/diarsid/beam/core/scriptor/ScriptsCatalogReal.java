/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.scriptor;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.config.Configuration;

/**
 *
 * @author Diarsid
 */
public class ScriptsCatalogReal implements ScriptsCatalog {
    
    ScriptsCatalogReal(Configuration configuration, String path) {
    }
    
    public static ScriptsCatalog getScriptsCatalog(Configuration configuration) {
        return new ScriptsCatalogReal(configuration, "./");
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

    @Override
    public void rewriteScripts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
