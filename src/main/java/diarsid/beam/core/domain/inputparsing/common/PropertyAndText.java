/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import diarsid.beam.core.domain.entities.metadata.EntityProperty;

/**
 *
 * @author Diarsid
 */
public class PropertyAndText {
    
    private final EntityProperty property;
    private final String text;

    public PropertyAndText(EntityProperty property, String text) {
        this.property = property;
        this.text = text;
    }

    public EntityProperty property() {
        return this.property;
    }

    public String text() {
        return this.text;
    }
}
