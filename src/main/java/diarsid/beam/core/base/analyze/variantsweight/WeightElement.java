/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import static java.lang.String.format;

import static diarsid.beam.core.base.analyze.variantsweight.WeightElement.WeightType.CALCULATED;
import static diarsid.beam.core.base.analyze.variantsweight.WeightElement.WeightType.PREDEFINED;

/**
 *
 * @author Diarsid
 */
public enum WeightElement {
    
    CHAR_IS_ONE_CHAR_WORD(
            -19.2, "char is one-char-word"),
    PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START(
            -17.71, "previous char is word separator, current char is at pattern start!"),
    PREVIOUS_CHAR_IS_SEPARATOR(
            -3.1, "previous char is word separator"),
    CLUSTER_BEFORE_SEPARATOR(
            -10.5, "there is cluster before separator!"),
    CLUSTER_STARTS_WITH_VARIANT(
            -6.6, "cluster starts with variant"),
    CLUSTER_STARTS_PREVIOUS_CHAR_IS_WORD_SEPARATOR(
            -6.6, "cluster start, previous char is word separator"),
    CLUSTER_STARTS_CURRENT_CHAR_IS_WORD_SEPARATOR(
            -6.6, "cluster start, current char is word separator"),
    NEXT_CHAR_IS_SEPARATOR(
            -3.1, "next char is word separator"),
    CLUSTER_ENDS_CURRENT_CHAR_IS_WORD_SEPARATOR(
            -6.6, "cluster ends, current char is word separator"),
    
    PATTERN_CONTAINS_CLUSTER(
            "pattern contains cluster"),
    PATTERN_CONTAINS_CLUSTER_LONG_WORD(
            "pattern contains cluster and it is a long word"),
    PATTERN_DOES_NOT_CONTAIN_CLUSTER(
            "pattern DOES NOT contain cluster"),
    CLUSTERS_NEAR_ARE_IN_ONE_PART(
            "clusters near, are in one part"),
    CHAR_BEFORE_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD(
            "char before previous separator and cluster enclosing single word"),
    CLUSTER_IS_WORD(
            "cluster is a word"),
    UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START(
            "unnatural positioning - cluster found at end but pattern cluster at start"),
    UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END(
            "unnatural positioning - cluster found at start but pattern cluster at end"),
    PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD(
            "previous cluster and current char belong to one word"),
    CLUSTERS_ORDER_INCOSISTENT(
            "clusters order incosistency"),
    CLUSTERS_ARE_WEAK_2_LENGTH(
            "all clusters are weak (2 length)"),
    PLACING_PENALTY(
            "placing penalty"), 
    PLACING_BONUS(
            "placing bonus"),
    CLUSTER_IS_CONSISTENT(
            "for consistency"),
    CLUSTER_IS_NOT_CONSISTENT(
            "for inconsistency"),
    CLUSTER_HAS_SHIFTS(
            "for shifts"),
    CLUSTER_ENDS_WITH_VARIANT(
            "cluster ends with variant"),
    CLUSTER_ENDS_NEXT_CHAR_IS_WORD_SEPARATOR(
            "cluster ends, next char is word separator"),
    SINGLE_POSITIONS_DENOTE_WORD(
            "single positions denote word");
    
    static enum WeightType {
        CALCULATED,
        PREDEFINED;
    }
    
    private final double predefinedWeight;
    private final String description;
    private final WeightType type;

    WeightElement(String description) {
        this.predefinedWeight = 0;
        this.description = description;
        this.type = CALCULATED;
    }

    WeightElement(double predefinedWeight, String description) {
        this.predefinedWeight = predefinedWeight;
        this.description = description;
        this.type = PREDEFINED;
    }
    
    double predefinedWeight() {
        return this.predefinedWeight;
    }
    
    String description() {
        return this.description;
    }
    
    void weightTypeMustBe(WeightType someType) {
        if ( ! this.type.equals(someType) ) {
            throw new IllegalArgumentException(format(
                    "%s type expected, but was %s", this.type, someType));
        }
    }
}
