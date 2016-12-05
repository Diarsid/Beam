/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Comparator;

import static diarsid.beam.core.util.StringUtils.countSpaces;


/**
 *
 * @author Diarsid
 */
public class CommandsStringsComparator implements Comparator<String> {
    
    public CommandsStringsComparator() {
    }
    
    @Override
    public int compare(String s1, String s2) {
        if ( s1.length() < s2.length() ) {
            return -1;
        } else if ( s1.length() > s2.length() ) {
            return 1;
        } else {
            return this.compareSpaces(s1, s2);
        }
    }

    private int compareSpaces(String s1, String s2) {
        int s1Spaces = countSpaces(s1);
        int s2Spaces = countSpaces(s2);
        if ( s1Spaces > s2Spaces ) {
            return 1;            
        } else if ( s1Spaces < s2Spaces ) {
            return -1;
        } else {
            return this.compareHashCodes(s1, s2);
        }
    }
    
    private int compareHashCodes(String s1, String s2) {
        if ( s1.hashCode() < s2.hashCode() ) {
            return -1;
        } else if ( s1.hashCode() > s2.hashCode() ) {
            return 1;
        } else {
            return 0;
        }
    }
}
