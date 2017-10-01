/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.time.Year.isLeap;
import static java.util.Arrays.asList;

import static diarsid.beam.core.domain.inputparsing.time.TimeType.NEXT_MONTH_DAY_HOURS_AND_MINUTES;


class ParserForNextMonthDayHourMinutePattern implements TimePatternParser {
    
    ParserForNextMonthDayHourMinutePattern() {
    }
    
    @Override
    public List<String> timePatterns() {
        return asList(
                "current or next year month and time:",
                "  09.01 21:35",
                "  09m-01d 21h:35m",
                "  9.1 7:12");
    }

    @Override
    public Optional<Time> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}m?(\\.|-)\\d{1,2}d? \\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.replace("d", "").replace("h", "").replace("m", "");
            int spaceIndex = timePattern.indexOf(" ");
            int semicolonIndex = timePattern.indexOf(":");
            int monthDaySeparator;
            if ( timePattern.contains("-") ) {
                monthDaySeparator = timePattern.indexOf("-");
            } else {
                monthDaySeparator = timePattern.indexOf(".");
            }
            int days = parseInt(timePattern.substring(0, monthDaySeparator));
            int months = parseInt(timePattern.substring(monthDaySeparator + 1, spaceIndex));
            int hours = parseInt(timePattern.substring(spaceIndex + 1, semicolonIndex));
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            LocalDateTime now = now();
            int thisMonthLength = now.getMonth().length(isLeap(now.getYear()));
            if ( 
                    months > 0 && months < 13 &&
                    days > 0 && days <= thisMonthLength && 
                    hours >= 0 && hours < 24 && 
                    minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withMonth(months)
                        .withDayOfMonth(days)
                        .withHour(hours)
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new Time(time, NEXT_MONTH_DAY_HOURS_AND_MINUTES));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
