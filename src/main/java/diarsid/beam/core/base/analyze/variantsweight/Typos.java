/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;

import diarsid.support.objects.Pool;
import diarsid.support.objects.Possible;
import diarsid.support.objects.StatefulClearable;

import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.beam.core.base.analyze.variantsweight.Typos.Placing.BEFORE;
import static diarsid.beam.core.base.analyze.variantsweight.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.Possibles.possibleButEmpty;


/**
 *
 * @author Diarsid
 */
class Typos implements StatefulClearable, AutoCloseable {
    
    static enum Placing {
        BEFORE,
        AFTER
    }
    
    private final Pool<Typo> typosPool;
    private final Possible<String> pattern;
    private final Possible<String> variant;
    private final List<Typo> typosBefore;
    private final List<Typo> typosAfter;

    Typos() {
        this.typosPool = pools().createPool(Typo.class, () -> new Typo());
        this.pattern = possibleButEmpty();
        this.variant = possibleButEmpty();
        this.typosBefore = new ArrayList<>();
        this.typosAfter = new ArrayList<>();
    }
    
    void set(String variant, String pattern) {
        this.variant.resetTo(variant);
        this.pattern.resetTo(pattern);
    }
    
    void findIn(
            Typos.Placing placing,
            int variantFromIncl, int variantToExcl, 
            int patternFromIncl, int patternToExcl) {
        logAnalyze(POSITIONS_SEARCH, "          [info] typo searching:");
        logAnalyze(POSITIONS_SEARCH, "             in variant from incl. %s to excl %s - %s", 
                                     variantFromIncl, 
                                     variantToExcl, 
                                     this.variant.orThrow().substring(variantFromIncl, variantToExcl));
        logAnalyze(POSITIONS_SEARCH, "             in pattern from incl. %s to excl %s - %s", 
                                     patternFromIncl, 
                                     patternToExcl, 
                                     this.pattern.orThrow().substring(patternFromIncl, patternToExcl));
        if ( variantFromIncl < 0 ) {
            throw new IllegalArgumentException();
        } 
        if ( patternFromIncl < 0 ) {
            throw new IllegalArgumentException();
        }
        if ( variantFromIncl >= variantToExcl ) {
            throw new IllegalArgumentException();
        }
        if ( patternFromIncl >= patternToExcl ) {
            throw new IllegalArgumentException();
        }
        
        String variantString = variant.orThrow();
        String patternString = pattern.orThrow();        
        int variantLength = variantString.length();
        int patternLength = patternString.length();
        
        if ( variantToExcl > variantLength ) {
            throw new IllegalArgumentException();
        }
        if ( patternToExcl > patternLength ) {
            throw new IllegalArgumentException();
        }
                
        char charInVariant;
        char charInPattern;
        
        for (int vi = variantFromIncl; vi < variantToExcl && vi < variantLength; vi++) {
            charInVariant = variantString.charAt(vi);
            for (int pi = patternFromIncl; pi < patternToExcl && pi < patternLength; pi++) {
                charInPattern = patternString.charAt(pi);
                if ( charInVariant == charInPattern ) {
                    Typo typo = this.typosPool.give();
                    typo.set(vi, pi, charInVariant);
                    if (placing == BEFORE) {
                        this.typosBefore.add(typo);
                    } else {
                        this.typosAfter.add(typo);
                    }
                }
            }
        }
    }
    
    int qtyBefore() {
        return this.typosBefore.size();
    }
    
    int qtyAfter() {
        return this.typosAfter.size();
    }
    
    int qtyTotal() {
        return this.qtyBefore() + this.qtyAfter();
    }
    
    boolean hasBefore() {
        return this.typosBefore.size() > 0;
    }
    
    boolean hasAfter() {
        return this.typosAfter.size() > 0;
    }
    
    boolean hasInBefore(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.typosBefore);
    }
    
    boolean hasInAfter(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.typosAfter);
    }
    
    private static boolean hasIndexInTypos(int variantIndex, List<Typo> typos) {
        Typo typo;
        for (int i = 0; i < typos.size(); i++) {
            typo = typos.get(i);
            if ( typo.variantIndex() == variantIndex ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Typos{" + "typosBefore=" + typosBefore + ", typosAfter=" + typosAfter + '}';
    }

    @Override
    public void clear() {
        this.pattern.nullify();
        this.variant.nullify();
        this.typosPool.takeBackAll(this.typosBefore);
        this.typosPool.takeBackAll(this.typosAfter);
    }

    @Override
    public void close() {
        this.clear();
    }
    
}
