/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import diarsid.beam.core.util.Pair;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import static diarsid.beam.core.util.Logs.log;

/**
 *
 * @author Diarsid
 */
class ApplicationConfigurationReading {
    
    private ApplicationConfigurationReading() {
    }
    
    static Stream<String> readConfigEntriesAsLinesFrom(Path configFile) {
        try {
        return Files
                .lines(configFile, Charset.forName("UTF-8"))
                .filter(line -> ! line.isEmpty())
                .filter(line -> line.contains("=")) 
                .map(line -> skipBomIfPresent(line))                               
                .filter(line -> isConfigLine(line))
                .map(line -> clean(line));
        } catch (IOException e) {
            log(ApplicationConfigurationReading.class, 
                    format("Config file '%s' not found. Default config will be used.", configFile.toString()));
            return Stream.empty();
        }
    }
    
    static Configuration parse(String... lines) {
        return parse(stream(lines));
    }
    
    static Configuration parse(Stream<String> lines) {
        final Map<String, String> singleOptions = new HashMap<>();
        final Map<String, List<String>> multipleOptions = new HashMap<>();
        lines
                .map(line -> parsePair(line))
                .forEach(pair -> mergePairIntoMaps(pair, singleOptions, multipleOptions));
        return new Configuration(singleOptions, multipleOptions);
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
        if ( line.contains(" = ") ) {
            return splitToPair(line, " = ");
        } else {
            return splitToPair(line, "=");
        } 
    }

    private static Pair<String, String> splitToPair(String line, String dilimiter) {
        line = line.trim();
        return new Pair<>(
                line.substring(0, line.indexOf(dilimiter)),
                line.substring(line.indexOf(dilimiter) + dilimiter.length(), line.length()));
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
