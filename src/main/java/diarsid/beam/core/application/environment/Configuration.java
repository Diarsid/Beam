/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.base.util.Logs.log;

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
    
    void logAll() {
        options.entrySet()
                .stream()
                .map((entry) -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String valueString;
                    
                    if ( value instanceof String ) {
                        valueString = (String) value;
                    } else if ( value instanceof List ) {
                        valueString = ((List<String>) value).stream().collect(joining(" "));
                    } else {
                        valueString = value.toString();
                    }
                    
                    return key + " = " + valueString;
                })
                .sorted()
                .forEach(line -> log(Configuration.class, line));
                
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
        return 
                this.has(option) && 
                this.options.get(option) instanceof String;
    } 
    
    public boolean hasList(String option) {
        return 
                this.has(option) && 
                this.options.get(option) instanceof List;
    }
    
    public String asString(String option) {
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
    
    public boolean asBoolean(String option) {
        if ( this.has(option) ) {
            Object config = this.options.get(option);
            if ( config instanceof String ) {
                return parseBoolean((String) config);
            } else {
                throw new IllegalArgumentException(
                        format("'%s' option is not String.", option));
            }
        } else {
            return false;
        }
    }
    
    public List<String> asList(String option) {
        if ( this.has(option) ) {
            Object config = this.options.get(option);
            if ( config instanceof String ) {
                return Arrays.asList((String) config);
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
