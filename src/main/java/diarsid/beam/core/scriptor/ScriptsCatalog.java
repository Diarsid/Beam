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
public interface ScriptsCatalog {

    List<Script> getScripts();

    Optional<Script> getScriptByName();

    String getCatalogPath();

    void rewriteScripts();
}
