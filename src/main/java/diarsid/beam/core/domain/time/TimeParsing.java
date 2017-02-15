/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.time;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 *
 * @author Diarsid
 */
public class TimeParsing {
    
    private final static Set<TimePatternDetector> DETECTORS;
    private final static Set<TimePatternParser> PARSERS;
    
    static {
        DETECTORS = new HashSet<>();
        PARSERS = new HashSet<>();
        
        Set<TimePatternParser> allDefined = new HashSet<>(asList(
                new ParserForPlusHourMinutePattern(),
                new ParserForPlusHoursPattern(),
                new ParserForPlusMinutesPattern(),
                new ParserForNextHourPattern(),
                new ParserForNextMinutePattern(),
                new ParserForNextHourMinutePattern(),
                new ParserForNextDaysHoursMinutesPattern(),
                new ParserForNextMonthDayHourMinutPattern(),
                new ParserForFullDatePattern()
        ));
        
        DETECTORS.addAll(allDefined);
        PARSERS.addAll(allDefined);
    }
    
    private TimeParsing() {
    }
    
    public static TimePatternDectectorsHolder dectectorsHolder() {
        return new TimePatternDectectorsHolder(DETECTORS);
    }
    
    public static TimePatternParsersHolder parsersHolder() {
        return new TimePatternParsersHolder(PARSERS);
    }
}
