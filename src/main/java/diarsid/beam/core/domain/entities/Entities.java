/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

/**
 *
 * @author Diarsid
 */
public class Entities {
    
    private Entities() {        
    }

    public static Program asProgram(NamedEntity entity) {
        return (Program) entity;
    }

    public static WebPage asWebPage(NamedEntity entity) {
        return (WebPage) entity;
    }

    public static Location asLocation(NamedEntity entity) {
        return (Location) entity;
    }

    public static Batch asBatch(NamedEntity entity) {
        return (Batch) entity;
    }
}
