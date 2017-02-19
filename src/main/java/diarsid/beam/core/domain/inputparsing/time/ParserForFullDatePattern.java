/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.time;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.time.Year.isLeap;

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.FULL_DATE;


class ParserForFullDatePattern implements TimePatternParser {
    
    ParserForFullDatePattern() {
    }

    @Override
    public Optional<TaskTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{4}y?(\\.|-)\\d{1,2}m?(\\.|-)\\d{1,2}d? \\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.replace("d", "").replace("h", "").replace("m", "").replace("y", "");
            int spaceIndex = timePattern.indexOf(" ");
            int semicolonIndex = timePattern.indexOf(":");
            int monthDaySeparator;
            int yearMonthSeparator;
            if ( timePattern.contains("-") ) {
                yearMonthSeparator = timePattern.indexOf("-");
                monthDaySeparator = timePattern.indexOf("-", yearMonthSeparator + 1);                
            } else {
                yearMonthSeparator = timePattern.indexOf(".");
                monthDaySeparator = timePattern.indexOf(".", yearMonthSeparator + 1);    
            }
            int year = parseInt(timePattern.substring(0, yearMonthSeparator));
            int months = parseInt(timePattern.substring(yearMonthSeparator + 1, monthDaySeparator));
            int days = parseInt(timePattern.substring(monthDaySeparator + 1, spaceIndex));
            
            int hours = parseInt(timePattern.substring(spaceIndex + 1, semicolonIndex));
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            LocalDateTime now = now();
            int thisMonthLength = now.getMonth().length(isLeap(now.getYear()));
            if ( 
                    year > 1969 &&
                    months > 0 && months < 13 &&
                    days > 0 && days <= thisMonthLength && 
                    hours >= 0 && hours < 24 && 
                    minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withYear(year)
                        .withMonth(months)
                        .withDayOfMonth(days)
                        .withHour(hours)
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TaskTime(time, FULL_DATE));
            } else {
                return Optional.empty();
            }
        } else if ( timePattern.trim().matches("\\d{1,2}d?(\\.|-)\\d{1,2}m?(\\.|-)\\d{4}y? \\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.replace("d", "").replace("h", "").replace("m", "").replace("y", "");
            int spaceIndex = timePattern.indexOf(" ");
            int semicolonIndex = timePattern.indexOf(":");
            int dayMonthSeparator;
            int monthYearSeparator;
            if ( timePattern.contains("-") ) {
                dayMonthSeparator = timePattern.indexOf("-");
                monthYearSeparator = timePattern.indexOf("-", dayMonthSeparator + 1);
            } else {
                dayMonthSeparator = timePattern.indexOf(".");
                monthYearSeparator = timePattern.indexOf(".", dayMonthSeparator + 1);
            }
            int days = parseInt(timePattern.substring(0, dayMonthSeparator));
            int months = parseInt(timePattern.substring(dayMonthSeparator + 1, monthYearSeparator));
            int year = parseInt(timePattern.substring(monthYearSeparator + 1, spaceIndex));
            
            int hours = parseInt(timePattern.substring(spaceIndex + 1, semicolonIndex));
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            LocalDateTime now = now();
            int thisMonthLength = now.getMonth().length(isLeap(now.getYear()));
            if ( 
                    year > 1969 &&
                    months > 0 && months < 13 &&
                    days > 0 && days <= thisMonthLength && 
                    hours >= 0 && hours < 24 && 
                    minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withYear(year)
                        .withMonth(months)
                        .withDayOfMonth(days)
                        .withHour(hours)
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TaskTime(time, FULL_DATE));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
