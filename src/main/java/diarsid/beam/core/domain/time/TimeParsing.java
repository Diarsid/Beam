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
    
    private final static TimePatternDectectorsHolder DETECTORS_HOLDER;
    private final static TimePatternParsersHolder PARSERS_HOLDER;
    private final static AllowedExecutionTimeParser ALLOWED_TIME_PARSER;
    
    static {        
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
        
        DETECTORS_HOLDER = new TimePatternDectectorsHolder(allDefined);
        PARSERS_HOLDER = new TimePatternParsersHolder(allDefined);
        
        ALLOWED_TIME_PARSER = new AllowedExecutionTimeParser();
    }
    
    private TimeParsing() {
    }
    
    public static TimePatternDectectorsHolder dectectorsHolder() {
        return DETECTORS_HOLDER;
    }
    
    public static TimePatternParsersHolder parsersHolder() {
        return PARSERS_HOLDER;
    }
    
    public static AllowedExecutionTimeParser allowedExecutionTimeParser() {
        return ALLOWED_TIME_PARSER;
    }
}
