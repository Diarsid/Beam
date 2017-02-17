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

import static diarsid.beam.core.domain.inputparsing.time.TasksTimeType.NEXT_HOURS;


class ParserForNextHourPattern implements TimePatternParser {
    
    ParserForNextHourPattern() {
    }

    private int parseHours(String timePattern) {
        return parseInt(timePattern.trim().replace("h", ""));
    }

    @Override
    public Optional<TasksTime> parse(String timePattern) {
        if ( timePattern.trim().matches("\\d{1,2}h") ) {
            int hours = this.parseHours(timePattern);
            if ( hours >= 0 && hours < 24 ) {
                LocalDateTime time = now()
                        .withHour(hours)
                        .withSecond(0)
                        .withNano(0);
                return Optional.of(new TasksTime(time, NEXT_HOURS));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }            
    }
}
