/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 *
 * @author Diarsid
 */
public class TimeParsing {
    
    private final static TimeParser PARSERS_HOLDER;
    private final static AllowedTimePeriodsParser ALLOWED_TIME_PARSER;
    
    static {        
        Set<TimePatternParser> allDefined = new HashSet<>(asList(
                new ParserForPlusHourMinutePattern(),
                new ParserForPlusHoursPattern(),
                new ParserForPlusMinutesPattern(),
                new ParserForNextHourPattern(),
                new ParserForNextMinutePattern(),
                new ParserForNextHourMinutePattern(),
                new ParserForNextDaysHoursMinutesPattern(),
                new ParserForNextMonthDayHourMinutePattern(),
                new ParserForFullDatePattern()
        ));
        
        PARSERS_HOLDER = new TimeParser(allDefined);        
        ALLOWED_TIME_PARSER = new AllowedTimePeriodsParser();
    }
    
    private TimeParsing() {
    }
    
    public static AllowedTimePeriodsParser allowedTimePeriodsParser() {
        return ALLOWED_TIME_PARSER;
    }
    
    public static TimeParser timePatternParsersHolder() {
        return PARSERS_HOLDER;
    }
}
