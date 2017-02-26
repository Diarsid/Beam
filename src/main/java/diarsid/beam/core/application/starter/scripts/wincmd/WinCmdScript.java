/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.starter.scripts.wincmd;

import java.util.List;

import diarsid.beam.core.application.starter.scripts.Script;
import diarsid.beam.core.application.catalogs.ScriptsCatalog;

/**
 *
 * @author Diarsid
 */
public class WinCmdScript implements Script {
    
    private final String name;
    private final List<String> lines;
    private final ScriptsCatalog catalog;
    
    public WinCmdScript(String name, List<String> lines, ScriptsCatalog catalog) {
        this.name = name;
        this.lines = lines;
        this.catalog = catalog;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void saveInScriptsCatalog() {
        this.catalog.getCatalogPath();
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getLines() {
        return this.lines;
    }
}
