/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Comparator;

/**
 *
 * @author Diarsid
 */
public class StringByLengthComparator implements Comparator<String> {
    
    public StringByLengthComparator() {
    }
    
    @Override
    public int compare(String s1, String s2) {
        if ( s1.length() < s2.length() ) {
            return -1;
        } else if ( s1.length() > s2.length() ) {
            return 1;
        } else {
            if ( s1.hashCode() < s2.hashCode() ) {
                return -1;
            } else if ( s1.hashCode() > s2.hashCode() ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
