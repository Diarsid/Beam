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

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.PLUS_HOURS_AND_MINUTES;


class ParserForPlusHourMinutePattern implements TimePatternParser {
    
    ParserForPlusHourMinutePattern() {
    }

    @Override
    public Optional<TaskTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\+\\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.trim().replace("h", "").replace("m", "").substring(1);
            int semicolonIndex = timePattern.indexOf(":");
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            int hours = parseInt(timePattern.substring(0, semicolonIndex));
            if ( 
                    minutes >= 0 && minutes < 59 && 
                    hours > 0 && hours < 100 ) {
                LocalDateTime time = now()
                        .plusHours(hours)
                        .plusMinutes(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TaskTime(time, PLUS_HOURS_AND_MINUTES));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }       
    }
}
