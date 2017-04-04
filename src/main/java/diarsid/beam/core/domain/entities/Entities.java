/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Optional;

/**
 *
 * @author Diarsid
 */
public class Entities {
    
    private Entities() {        
    }

    public static Program asProgram(Optional<? extends NamedEntity> entity) {
        return (Program) entity.get();
    }

    public static WebPage asWebPage(Optional<? extends NamedEntity> entity) {
        return (WebPage) entity.get();
    }

    public static Location asLocation(Optional<? extends NamedEntity> entity) {
        return (Location) entity.get();
    }

    public static Batch asBatch(Optional<? extends NamedEntity> entity) {
        return (Batch) entity.get();
    }
}
