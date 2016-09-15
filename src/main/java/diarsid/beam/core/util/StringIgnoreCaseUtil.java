/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Diarsid
 */
public class StringIgnoreCaseUtil {
    
    private StringIgnoreCaseUtil() {
    }
        
    
    public static boolean containsAllPartsIgnoreCase(String whereToSearch, String searched) {
        for (String searchedPart : searched.split("-")) {
            if ( ! whereToSearch.toLowerCase().contains(searchedPart.toLowerCase()) ) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean containsIgnoreCase(String whereToSearch, String searched) {
        if ( searched == null || whereToSearch == null ) {
            return false;
        } else {
            return whereToSearch.toLowerCase().contains(searched.toLowerCase());
        }        
    }
    
    public static boolean containsIgnoreCase(Collection<String> whereToSearch, String searched) {
        if ( whereToSearch == null || whereToSearch.isEmpty() || searched == null ) {
            return false;
        } else {
            return whereToSearch.stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst()
                    .isPresent();
        }        
    }
    
    public static int indexOfIgnoreCase(List<String> whereToSearch, String searched) {
        if ( whereToSearch == null || whereToSearch.isEmpty() || searched == null ) {
            return -1;
        } else {
            Optional<String> optionalElement =  whereToSearch.stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst();
            if ( optionalElement.isPresent() ) {
                return whereToSearch.indexOf(optionalElement.get());
            } else {
                return -1;
            }
        }
    }
    
    public static boolean containsKeyIgnoreCase(Map<String, Object> mapToSearch, String searched) {
        if ( mapToSearch == null || mapToSearch.isEmpty() || searched == null ) {
            return false;
        } else {
            return mapToSearch.keySet().stream()
                    .filter(s -> s.equalsIgnoreCase(searched))
                    .findFirst()
                    .isPresent();
        }        
    }
    
    public static <T> T getIgnoreCase(Map<String, T> mapToSearch, String keyIgnoreCase) {
        if ( mapToSearch == null || mapToSearch.isEmpty() || keyIgnoreCase == null ) {
            return null;
        } else {
            Optional<String> optionalKey = mapToSearch.keySet().stream()
                    .filter(s -> s.equalsIgnoreCase(keyIgnoreCase))
                    .findFirst();
            if ( optionalKey.isPresent() ) {
                return mapToSearch.get(optionalKey.get());
            } else {
                return null;
            }
        }    
    }
}