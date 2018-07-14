/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

import static diarsid.beam.core.application.environment.ConfigurationReading.parseConfigLines;
import static diarsid.beam.core.application.environment.ConfigurationReading.readConfigEntriesAsLinesFrom;
import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */
public class Configuration {
    
    private static final Configuration DEFAULT_CONFIGURATION;
    private static final Configuration ACTUAL_CONFIGURATION;
    
    static {
        DEFAULT_CONFIGURATION = parseConfigLines(
                "data.store = ../res/data",
                "data.driver = org.h2.Driver",
                "data.user = root",
                "data.pass = root",
                "data.log = true",
                "data.access.version = 2",
                "catalogs.programs = ../env/programs",
                "catalogs.notes = ../env/notes",
                "filesystem.executables = ",
                "filesystem.program.specific.files = ",
                "filesystem.program.specific.folders = ",
                "filesystem.project.definitive.files = ",
                "filesystem.project.definitive.folders = ",
                "filesystem.project.specific.files = ",
                "filesystem.project.specific.folders = ",
                "filesystem.restricted.folders = ",
                "ui.images.resources = ../res/images/",
                "ui.images.capture.webpages.resize = true",
                "ui.console.runOnStart = true",
                "analyze.weight.base.log = true",
                "analyze.weight.positions.search.log = false",
                "analyze.weight.positions.clusters.log = true",
                "analyze.result.variants.limit = 11",
                "analyze.similarity.log.base = true",
                "analyze.similarity.log.advanced = true",
                "web.local.host = 127.0.0.1",
                "web.local.port = 32001",
                "web.local.path = /beam/core",
                "web.local.resources = ../res/static_web_context",
                "rmi.core.active = true",
                "rmi.core.port = 43006",
                "rmi.core.host = 127.0.0.1",
                "rmi.sysconsole.port = 43005",
                "rmi.sysconsole.host = 127.0.0.1",
                "core.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                "core.jvm.option = -Dfile.encoding=UTF-8",
                "core.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                "core.jvm.option = -Xms32m",
                "core.jvm.option = -Xmx32m",
                "sysconsole.jvm.option = -Djava.rmi.server.hostname=127.0.0.1",
                "sysconsole.jvm.option = -Dfile.encoding=UTF-8",
                "sysconsole.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties",
                "sysconsole.jvm.option = -Xms4m",
                "sysconsole.jvm.option = -Xmx4m",
                "starter.jvm.option = -Xmx32m",
                "starter.jvm.option = -Xms32m",
                "starter.jvm.option = -Dfile.encoding=UTF-8",
                "starter.jvm.option = -Dlog4j.configuration=file:../config/log4j.properties");
        
        Path configFile = Paths.get("../config/beam.config");
        ACTUAL_CONFIGURATION = parseConfigLines(readConfigEntriesAsLinesFrom(configFile));
        ACTUAL_CONFIGURATION.takeUnconfiguredFrom(DEFAULT_CONFIGURATION);
        ACTUAL_CONFIGURATION.logAll();
    }
    
    // Object value may be String or List<String>
    private final Map<String, Object> options;
    
    Configuration(Map<String, Object> options) {
        this.options = options;
    }
    
    static Configuration defaultConfiguration() {
        return DEFAULT_CONFIGURATION;
    }
    
    static Configuration actualConfiguration() {
        return ACTUAL_CONFIGURATION;
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
    
    void takeUnconfiguredFrom(Configuration other) {
        other.options.forEach((key, value) -> {
            this.options.putIfAbsent(key, value);
        });
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
    
    public boolean hasInt(String option) {
        if ( this.hasString(option) ) {
            String found = (String) this.options.get(option);
            try {
                parseInt(found);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } 
        return false;
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
    
    public int asInt(String option) {
        if ( this.has(option) ) {
            Object config = this.options.get(option);
            if ( config instanceof String ) {
                return Integer.valueOf((String) config);
            } else if ( config instanceof List ) {
                throw new IllegalArgumentException(
                        format("Configured '%s' option has multiple values, " +
                                "cannot be converted to int.", option));
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
