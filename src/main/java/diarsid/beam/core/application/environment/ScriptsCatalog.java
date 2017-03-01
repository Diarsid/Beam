/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.util.List;
import java.util.Optional;


/**
 *
 * @author Diarsid
 */
public interface ScriptsCatalog extends Catalog {
    
    ScriptBuilder newScript(String name);

    List<Script> getScripts();
    
    boolean notContains(Script script);

    Optional<Script> getScriptByName(String name);
}
