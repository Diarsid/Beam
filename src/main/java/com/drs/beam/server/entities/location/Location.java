/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.server.entities.location;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Diarsid
 */
public class Location implements Serializable{
// ________________________________________________________________________________________
//                                        Fields                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

    private final String name;
    private final String path;
    
// ________________________________________________________________________________________
//                                     Constructor                                         
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    public Location(String name, String path) {
        this.name = name;
        this.path = path;
    }    
    
// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Location > " + this.name + " : " + this.path;
    }
    
    /**
     * Method for formatted locations printing. Allows to get location description String 
     * with specified length between the beginning of location name and ":" delimiter to 
     * get string of this format:
     *    loc1        : path/to/loc1
     *    exampleLoc  : another/path
     *    locName     : path/to/another/loc
     * Parameter int locationPartLength specified the span length. If parameter is less 
     * than or equal to locationName.length(), it will be increased by 10 until it became 
     * longer than locationName.length().     * 
     * 
     * @param locationPartLength specifies space between the beginning of formatted string 
     * and ":" delimiter.
     * @return Formatted string of format "locationName : locationPath", where space between 
     * the beginning of string and ":" delimiter is determined by parameter value.
     */
    public String printLocationInFormat(int locationPartLength){
        int length = this.name.length();
        while (locationPartLength <= length){
            locationPartLength += 10;
        }
        char[] result = Arrays.copyOf(this.name.toCharArray(), locationPartLength);
        Arrays.fill(result, this.name.length(), locationPartLength, ' ');  
        return String.copyValueOf(result) + ": " + this.path;
    } 

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }
}
