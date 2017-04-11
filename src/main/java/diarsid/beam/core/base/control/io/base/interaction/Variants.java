/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public class Variants {    
    
    private final List<Variant> variants;

    protected Variants() {
        this.variants = new ArrayList<>();
    }

    protected Variants(List<Variant> variants) {
        this.variants = variants;
    }
    
    public boolean isWeighted() {
        return false;
    }
    
}
