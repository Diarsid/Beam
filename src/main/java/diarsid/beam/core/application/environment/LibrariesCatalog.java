/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface LibrariesCatalog extends Catalog {
    
    List<String> getLibraries();
    
    List<String> getLibrariesWithAny(String... fragments);
}
