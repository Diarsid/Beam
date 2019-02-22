/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.Comparator;

import static diarsid.support.strings.StringUtils.countSpaces;


/**
 *
 * @author Diarsid
 */
class StringsLengthComparator implements Comparator<String> {
    
    final static int SHORTER_STRINGS_FIRST;
    final static int LONGER_STRINGS_FIRST;
    
    static {
        SHORTER_STRINGS_FIRST = 0;
        LONGER_STRINGS_FIRST = 1;
    }
    
    private final int lengthComparation;
    private final boolean compareSpaces;
    
    StringsLengthComparator(int lengthComparation, boolean compareSpaces) {
        this.lengthComparation = lengthComparation;
        this.compareSpaces = compareSpaces;
    }
    
    @Override
    public int compare(String s1, String s2) {
        if ( this.lengthComparation == SHORTER_STRINGS_FIRST ) {
            if ( s1.length() < s2.length() ) {
                return -1;
            } else if ( s1.length() > s2.length() ) {
                return 1;
            } else {
                return compareSpacesIfAllowed(s1, s2);
            }
        } else {
            if ( s1.length() < s2.length() ) {
                return 1;
            } else if ( s1.length() > s2.length() ) {
                return -1;
            } else {
                return compareSpacesIfAllowed(s1, s2);
            }
        }        
    }

    private int compareSpacesIfAllowed(String s1, String s2) {
        if ( this.compareSpaces ) {
            int s1Spaces = countSpaces(s1);
            int s2Spaces = countSpaces(s2);
            if ( s1Spaces > s2Spaces ) {
                return 1;            
            } else if ( s1Spaces < s2Spaces ) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }            
    }
}
