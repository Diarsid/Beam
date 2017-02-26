/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.configuration;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;

/**
 *
 * @author Diarsid
 */
public class Configuration {
    
    // Object value may be String or List<String>
    private final Map<String, Object> options;
    
    Configuration(Map<String, Object> options) {
        this.options = options;
    }
    
    Configuration merge(Configuration other) {
        other.options.forEach((key, value) -> {
            this.options.putIfAbsent(key, value);
        });
        return this;
    }

    private boolean has(String option) {
        return this.options.containsKey(option);
    }
    
    public boolean hasString(String option) {
        if ( this.has(option) ) {
            return this.options.get(option) instanceof String;
        } else {
            throw new IllegalArgumentException(
                    format("There isn't configured '%s' option.", option));
        }
    } 
    
    public boolean isList(String option) {
        if ( this.has(option) ) {
            return this.options.get(option) instanceof List;
        } else {
            throw new IllegalArgumentException(
                    format("There isn't configured '%s' option.", option));
        }
    }
    
    public String getAsString(String option) {
        if ( this.has(option) ) {
            Object config = this.options.get(option);
            if ( config instanceof String ) {
                return (String) config;
            } else if ( config instanceof List ) {
                return join(" ", ((List<String>) config));
            } else {
                throw new IllegalArgumentException(
                        format("Unknown configured '%s' option type.", option));
            }
        } else {
            throw new IllegalArgumentException(
                    format("There isn't configured '%s' option.", option));
        }
    }
    
    public List<String> getAsList(String option) {
        if ( this.has(option) ) {
            Object config = this.options.get(option);
            if ( config instanceof String ) {
                return asList((String) config);
            } else if ( config instanceof List ) {
                return (List<String>) config;
            } else {
                throw new IllegalArgumentException(
                        format("Unknown configured '%s' option type.", option));
            }
        } else {
            throw new IllegalArgumentException(
                    format("There isn't configured '%s' option.", option));
        }
    }
}
