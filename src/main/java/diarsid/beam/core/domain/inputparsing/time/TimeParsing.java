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
    
    private final static TimePatternParsersHolder PARSERS_HOLDER;
    private final static AllowedTimePeriodsParser ALLOWED_TIME_PARSER;
    private final static TimeAndTextParser TIME_AND_TEXT_PARSER;
    
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
        
        PARSERS_HOLDER = new TimePatternParsersHolder(allDefined);        
        ALLOWED_TIME_PARSER = new AllowedTimePeriodsParser();
        TIME_AND_TEXT_PARSER = new TimeAndTextParser(PARSERS_HOLDER);
    }
    
    private TimeParsing() {
    }
    
    public static AllowedTimePeriodsParser allowedTimePeriodsParser() {
        return ALLOWED_TIME_PARSER;
    }
    
    public static TimePatternParsersHolder timePatternParsersHolder() {
        return PARSERS_HOLDER;
    }
    
    public static TimeAndTextParser timeAndTextParser() {
        return TIME_AND_TEXT_PARSER;
    }
}
