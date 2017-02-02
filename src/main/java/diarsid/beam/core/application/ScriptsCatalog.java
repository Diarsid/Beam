/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application;

import java.util.List;
import java.util.Optional;

import diarsid.beam.core.scriptor.Script;

/**
 *
 * @author Diarsid
 */
public interface ScriptsCatalog extends Catalog {

    List<Script> getScripts();

    Optional<Script> getScriptByName(String name);

    void rewriteScripts();
}
