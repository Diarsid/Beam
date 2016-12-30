/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 * @author Diarsid
 */
public class VariantAnswer implements Serializable {
    
    private static final VariantAnswer EMPTY_CHOICE;
    static {
        EMPTY_CHOICE = new VariantAnswer(null);
    }
    
    private final Variant chosen;
        
    private VariantAnswer(Variant chosen) {
        this.chosen = chosen;
    }
    
    public static VariantAnswer noAnswerFromVariants() {
        return EMPTY_CHOICE;
    }
    
    public static VariantAnswer answerOfVariant(Variant variant) {
        return new VariantAnswer(variant);
    }
    
    public boolean isPresent() {
        return nonNull(this.chosen);
    }
    
    public boolean isNotPresent() {
        return isNull(this.chosen);
    }
    
    public Variant get() {
        return this.chosen;
    }
}
