/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.Optional;

import diarsid.beam.core.util.Pair;

import static java.util.Objects.isNull;

/**
 *
 * @author Diarsid
 */
public class Attribute extends Pair<String, String> {
    
    public Attribute(String key, String value) {
        super(key, value);
    }
    
    private static Attribute nullableAttribute(String key, String value) {
        if ( isNull(key) || isNull(value) ) {
            return null;
        } else {
            return new Attribute(key, value);
        }
    }
    
    public static Optional<Attribute> optionalAttribute(
            String key, Optional<String> optionalValue) {
        return Optional.ofNullable(nullableAttribute(key, optionalValue.orElse(null)));
    }
    
    public String name() {
        return super.first();
    }
    
    public String content() {
        return super.second();
    }
}
