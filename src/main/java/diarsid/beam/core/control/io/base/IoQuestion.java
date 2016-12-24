/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public class IoQuestion implements Serializable {
    
    private final String question;
    private final List<Variant> variants;
    
    public IoQuestion(String question) {
        this.question = question;
        this.variants = new ArrayList<>();
    }
    
    public IoQuestion with(Variant variant) {
        this.variants.add(variant);
        return this;
    }
    
    public IoQuestion with(String variant) {
        this.variants.add(new Variant(variant));
        return this;
    }

    public String getQuestion() {
        return this.question;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }
    
}
