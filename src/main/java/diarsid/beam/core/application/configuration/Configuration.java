/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.join;

/**
 *
 * @author Diarsid
 */
public class Configuration {
    
    private final Map<String, String> singleOptions;
    private final Map<String, List<String>> multipleOptions;

    Configuration(
            Map<String, String> singleOptions, 
            Map<String, List<String>> multiOptions) {
        this.singleOptions = singleOptions;
        this.multipleOptions = multiOptions;
    }
    
    Configuration merge(Configuration other) {
        other.singleOptions.forEach((key, value) -> 
                this.singleOptions.putIfAbsent(key, value)
        );
        other.multipleOptions.forEach((key, value) -> 
                this.multipleOptions.putIfAbsent(key, value)
        );
        return this;
    }
    
    List<String> getAll() {
        List<String> list = new ArrayList();
        this.singleOptions.forEach((key, value) -> 
                list.add(key + " :: " + value)
        );
        this.multipleOptions.forEach((key, values) -> 
                list.add(key + " :: " + join(", ", values))
        );
        return list;
    }
    
    public boolean hasSingle(String option) {
        return this.singleOptions.containsKey(option);        
    }
    
    public boolean hasMultiple(String option) {
        return this.multipleOptions.containsKey(option);
    }
    
    public String getSingle(String option) {
        if ( this.singleOptions.containsKey(option) ) {
            return this.singleOptions.get(option);
        } else {
            throw new IllegalArgumentException(
                    format("There isn't single configured '%s' option.", option));
        }        
    }
    
    public List<String> getMultiple(String option) {
        if ( this.multipleOptions.containsKey(option) ) {
            return this.multipleOptions.get(option);
        } else {
            throw new IllegalArgumentException(
                    format("There isn't multiple configured '%s' option.", option));
        }
        
    }
    
    public String getJoinedMultiple(String option) {
        if ( this.multipleOptions.containsKey(option) ) {
            return join(" ", this.multipleOptions.get(option));
        } else {
            throw new IllegalArgumentException(
                    format("There isn't multiple configured '%s' option.", option));
        }
    }
    
    public String getJoinedMultiple(String option, String dilimiter) {
        if ( this.multipleOptions.containsKey(option) ) {
            return join(dilimiter, this.multipleOptions.get(option));
        } else {
            throw new IllegalArgumentException(
                    format("There isn't multiple configured '%s' option.", option));
        }
    }
}
