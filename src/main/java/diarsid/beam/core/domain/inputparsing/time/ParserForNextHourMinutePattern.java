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

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.NEXT_HOURS_AND_MINUTES;


class ParserForNextHourMinutePattern implements TimePatternParser {
    
    ParserForNextHourMinutePattern() {
    }

    @Override
    public Optional<TasksTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}h?:\\d{1,2}m?") ) {
            timePattern = timePattern.trim().replace("h", "").replace("m", "");
            int semicolonIndex = timePattern.indexOf(":");
            int minutes = parseInt(timePattern.substring(semicolonIndex + 1, timePattern.length()));
            int hours = parseInt(timePattern.substring(0, semicolonIndex));
            if ( 
                    hours >= 0 && hours < 24 && 
                    minutes >= 0 && minutes < 60 ) {
                LocalDateTime time = now()
                        .withHour(hours)
                        .withMinute(minutes)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TasksTime(time, NEXT_HOURS_AND_MINUTES));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
