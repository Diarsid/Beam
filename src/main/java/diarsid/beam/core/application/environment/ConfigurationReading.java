/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import diarsid.beam.core.base.util.Pair;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import static diarsid.beam.core.base.util.Logs.log;

/**
 *
 * @author Diarsid
 */
class ConfigurationReading {
    
    static final Configuration CONFIGURATION;
    
    static {
        Path configFile = Paths.get("../config/beam.config");
        Configuration defaultConfig = parse(
                "data.store = ../res/data",
                "data.driver = org.h2.Driver",
                "data.user = root",
                "data.pass = root",
                "data.log = true",
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
        Configuration actualConfig = parse(readConfigEntriesAsLinesFrom(configFile));
        CONFIGURATION = actualConfig.merge(defaultConfig);
        CONFIGURATION.logAll();
    }
    
    private ConfigurationReading() {
    }
    
    private static Stream<String> readConfigEntriesAsLinesFrom(Path configFile) {
        try {
        return Files
                .lines(configFile, Charset.forName("UTF-8"))
                .filter(line -> ! line.isEmpty())
                .filter(line -> line.contains("=")) 
                .map(line -> skipBomIfPresent(line))                               
                .filter(line -> isConfigLine(line))
                .map(line -> clean(line));
        } catch (IOException e) {
            log(ConfigurationReading.class, 
                    format("Config file '%s' not found. Default config will be used.", configFile.toString()));
            return Stream.empty();
        }
    }
    
    private static Configuration parse(String... lines) {
        return parse(stream(lines));
    }
    
    private static Configuration parse(Stream<String> lines) {
        Map<String, String> singleOptions = new HashMap<>();
        Map<String, List<String>> multipleOptions = new HashMap<>();
        lines
                .map(line -> parsePair(line))
                .forEach(pair -> mergePairIntoMaps(pair, singleOptions, multipleOptions));
        Map<String, Object> options = new HashMap<>();
        options.putAll(singleOptions);
        options.putAll(multipleOptions);
        return new Configuration(options);
    }
    
    private static void mergePairIntoMaps(
            Pair<String, String> pair, 
            Map<String, String> singleOptions, 
            Map<String, List<String>> multipleOptions) {
        if ( singleOptions.containsKey(pair.first()) && ! multipleOptions.containsKey(pair.first()) ) {
            List<String> list = new ArrayList<>();
            list.add(pair.second());
            list.add(singleOptions.get(pair.first()));
            multipleOptions.put(pair.first(), list);
            singleOptions.remove(pair.first());
        } else if ( ! singleOptions.containsKey(pair.first()) && multipleOptions.containsKey(pair.first()) ) {
            multipleOptions.get(pair.first()).add(pair.second());            
        } else {
            singleOptions.put(pair.first(), pair.second());
        }
    }
    
    private static Pair<String, String> parsePair(String line) {
        line = line.trim();
        return new Pair<>(
                line.trim().substring(0, line.indexOf("=")).trim(),
                line.trim().substring(line.indexOf("=") + "=".length(), line.length()).trim());
    }

    private static String clean(String line) {
        return line.trim().substring("[config] ".length());
    }

    private static boolean isConfigLine(String line) {
        return line.trim().startsWith("[config] ");
    }
    
    private static String skipBomIfPresent(String line) {
        if ( line.charAt(0) == '\ufeff' ) {
            return line.substring(1);
        } else {
            return line;
        }
    }
}
