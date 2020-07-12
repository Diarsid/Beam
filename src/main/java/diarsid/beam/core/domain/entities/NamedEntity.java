/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.domain.entities;

import diarsid.beam.core.base.analyze.variantsweight.ConvertableToVariant;

/**
 *
 * @author Diarsid
 */
public interface NamedEntity extends ConvertableToVariant {
    
    String name();
    
    NamedEntityType type();
    
    public default boolean is(NamedEntityType type) {
        return this.type().equals(type);
    }
}
