/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;

import diarsid.beam.core.base.objects.PooledReusable;


/**
 *
 * @author Diarsid
 */
public abstract class SqlPatternQuery extends PooledReusable {
    
    private final ModifiablePatternLikeness likeness;
    
    SqlPatternQuery() {
        super();
        this.likeness = new ModifiablePatternLikeness();
    }
    
    final ModifiablePatternLikeness likeness() {
        return this.likeness;
    }

    public final boolean isNextRequiredLikenessDecreaseMeaningfull() {
        return this.likeness.isNextDecreaseMeaningfull();
    }

    public abstract String compose();
    
    abstract void onLikenessDecreased(int oldMatches, int newMatches);
    
    public final SqlPatternQuery decreaseRequiredLikeness() {
        int oldRequiredMatches = this.likeness.requiredMatches();
        this.likeness.decrease();
        int newRequiredMatches = this.likeness.requiredMatches();
        if ( oldRequiredMatches == newRequiredMatches ) {
            return this;
        }
        this.onLikenessDecreased(oldRequiredMatches, newRequiredMatches);
        return this;
    }
    
}
